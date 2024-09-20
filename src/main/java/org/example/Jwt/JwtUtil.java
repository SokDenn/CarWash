package org.example.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилитарный класс для работы с JWT токенами.
 * Содержит методы для генерации, проверки и извлечения информации из токенов.
 */
@Component
public class JwtUtil {
    private static final String ROLES_KEY = "roles";

    //аннотация для внедрения значения секретного ключа из конфигурационного файла.
    @Value("${jwt.secret}")
    private String secret;
    // аннотация для внедрения значения времени истечения access токена из конфигурационного файла.
    @Value("${jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;
    //аннотация для внедрения значения времени истечения refresh токена из конфигурационного файла.
    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    /**
     * Сгенерировать JWT токен на основе данных пользователя.
     *
     * @param userDetails информация о пользователе
     * @param isAccessToken если true — сгенерировать access токен, иначе — refresh токен
     * @return сгенерированный JWT токен
     */
    public String generateToken(UserDetails userDetails, boolean isAccessToken) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), isAccessToken ? accessTokenExpiration : refreshTokenExpiration);
    }

    /**
     * Создать JWT токен с заданными данными и временем жизни.
     *
     * @param claims данные для токена
     * @param subject имя пользователя
     * @param expirationTime время жизни токена
     * @return сгенерированный токен
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) //установка времени истечения токена.
                .signWith(SignatureAlgorithm.HS256, secret) //подпись токена с использованием алгоритма HS256 и секретного ключа.
                .compact(); //создание компактного представления JWT в виде строки.
    }

    /**
     * Проверить корректность токена.
     *
     * @param token токен для проверки
     * @param userDetails данные пользователя
     * @return true, если токен валиден, иначе false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Извлечь имя пользователя из токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Извлечь роли пользователя из токена.
     *
     * @param token JWT токен
     * @return список ролей пользователя
     */
    public List<String> getRolesFromToken(String token) {
        return getAllClaimsFromToken(token).get(ROLES_KEY, List.class);
    }

    /**
     * Проверить, истек ли токен.
     *
     * @param token JWT токен
     * @return true, если токен истек, иначе false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Получить дату истечения токена.
     *
     * @param token JWT токен
     * @return дата истечения токена
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Извлечь определенное значение (claim) из токена.
     *
     * @param token JWT токен
     * @param claimsResolver функция для извлечения значения
     * @return извлеченное значение
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлечь все данные (claims) из токена.
     *
     * @param token JWT токен
     * @return все claims токена
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Создать cookie с access токеном.
     *
     * @param accessToken access токен
     * @return cookie с токеном
     */
    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(86400) // 1 день (1 * 24 * 60 * 60)
                .build();
    }

    /**
     * Сгенерировать токен для сброса пароля.
     *
     * @param username имя пользователя
     * @return токен для сброса пароля
     */
    public String generatePasswordResetToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        Long passwordResetExpiration = 60000L; // 5 минут (1 * 60 * 1000L)
        return createToken(claims, username, passwordResetExpiration);
    }

}
