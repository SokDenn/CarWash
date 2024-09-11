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

    public String generateToken(UserDetails userDetails, boolean isAccessToken) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), isAccessToken ? accessTokenExpiration : refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) //установка времени истечения токена.
                .signWith(SignatureAlgorithm.HS256, secret) //подпись токена с использованием алгоритма HS256 и секретного ключа.
                .compact(); //создание компактного представления JWT в виде строки.
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public List<String> getRolesFromToken(String token) {
        return getAllClaimsFromToken(token).get(ROLES_KEY, List.class);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {

        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(86400) // 1 день (1 * 24 * 60 * 60)
                .build();
    }
    public String generatePasswordResetToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        Long passwordResetExpiration = 60000L; // 5 минут (1 * 60 * 1000L)
        return createToken(claims, username, passwordResetExpiration);
    }

}
