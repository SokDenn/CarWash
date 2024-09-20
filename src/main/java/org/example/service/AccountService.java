package org.example.service;

import org.example.Jwt.JwtRequest;
import org.example.Jwt.JwtResponse;
import org.example.Jwt.JwtUtil;
import org.example.dto.UserDTO;
import org.example.model.User;
import org.example.security.DeleteAccountException;
import org.example.security.SecurityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для управления аккаунтами пользователей
 */
@Service
public class AccountService {
    @Autowired
    UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SecurityValidator securityValidator;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    ReservationStatusService reservationStatusService;
    @Autowired
    DiscountService discountService;
    @Autowired
    RoleService roleService;

    /**
     * Получить список всех активных пользователей и возможный диапазон скидок.
     *
     * @return объект UserDTO с данными о пользователях и скидках
     */
    public UserDTO getAllUsers() {
        UserDTO userDTO = new UserDTO();

        userDTO.setUserList(userService.findAllActive());
        userDTO.setDiscount(discountService.findDiscount());
        userDTO.setUserAuthentication(userService.getAuthenticationUser());

        return userDTO;
    }

    /**
     * Получить данные для редактирования указанного пользователя по его ID.
     *
     * @param userId идентификатор пользователя
     * @return объект UserDTO с данными о пользователе и ролях
     */
    public UserDTO getEditUsers(UUID userId) {
        UserDTO userDTO = new UserDTO();

        if (userId != null) {
            userDTO.setUser(userService.getUserById(userId));
        }
        userDTO.setRoleList(roleService.findAll());
        userDTO.setUserList(userService.findAllActive());

        return userDTO;
    }

    /**
     * Удалить аккаунт текущего пользователя, если это возможно.
     *
     * @return сообщение об успешном удалении аккаунта
     * @throws DeleteAccountException если пользователь пытается удалить аккаунт администратора или оператора
     */
    public String deleteAccount() {
        User currentUser = userService.getAuthenticationUser();
        String currentUserRoleStr = currentUser.getRole().getName();

        if (currentUserRoleStr.equals("ADMIN") || currentUserRoleStr.equals("OPERATOR")) {
            throw new DeleteAccountException("Удалить свой аккаунт может только пользователь!");

        }

        // Выполняем удаление пользователя и отмену бронирований
        userService.deleteUser(currentUser.getId());
        reservationStatusService.cancelledReservationFromUser(currentUser.getId());

        return "Аккаунт удален!";
    }

    /**
     * Аутентифицировать пользователя по предоставленным учетным данным.
     *
     * @param authenticationRequest объект JwtRequest с именем пользователя и паролем
     * @return объект JwtResponse с токенами доступа и куки
     */
    public JwtResponse authenticateUser(JwtRequest authenticationRequest) {
        try {
            Authentication authentication = securityValidator.authenticate(authenticationRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails, true);
            String refreshToken = jwtUtil.generateToken(userDetails, false);

            ResponseCookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);

            return new JwtResponse(accessToken, refreshToken, accessTokenCookie);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Некорректный логин или пароль");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сервера");
        }
    }

    /**
     * Обновить токены аутентификации пользователя с использованием refresh-токена.
     *
     * @param refreshRequest объект JwtRequest с refresh-токеном
     * @return объект JwtResponse с новыми токенами
     * @throws RuntimeException если refresh-токен недействителен
     */
    public JwtResponse updateAuthenticateUser(JwtRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getToken();

            String username = jwtUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                String accessToken = jwtUtil.generateToken(userDetails, true);
                ResponseCookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);

                return new JwtResponse(accessToken, refreshToken, accessTokenCookie);
            } else {
                throw new RuntimeException("Некорректный refresh token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сервера");
        }
    }

    /**
     * Отправить ссылку для восстановления пароля на основе имени пользователя.
     *
     * @param username имя пользователя, для которого необходимо восстановить пароль
     */
    public void passwordRecovery(String username) {
        String token = jwtUtil.generatePasswordResetToken(username);

        String resetLink = "http://localhost:8080/api/v1/auth/passwordReplacement?token=" + token;
        System.out.println("Ссылка для восстановления пароля: " + resetLink);
    }

    /**
     * Проверить, действителен ли указанный токен.
     *
     * @param token токен для проверки
     * @return true, если токен действителен; false в противном случае
     */
    public boolean isValidToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return username != null && jwtUtil.validateToken(token, userService.loadUserByUsername(username));
    }

    /**
     * Получить пользователя из токена.
     *
     * @param token токен, из которого необходимо извлечь пользователя
     * @return объект User, соответствующий токену
     */
    public User getUserFromToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return userService.findByUsername(username);
    }

    /**
     * Обновить пароль пользователя, если токен действителен.
     *
     * @param token токен для сброса пароля
     * @param newPassword новый пароль для пользователя
     * @return сообщение о результате операции
     */
    public String updatePassword(String token, String newPassword) {

        if (isValidToken(token)) {
            String username = getUserFromToken(token).getUsername();
            userService.updatePassword(username, newPassword);

            return "Ваш пароль был успешно изменен.";

        } else {
            return "Недействительная или истекшая ссылка для сброса пароля.";
        }
    }
}
