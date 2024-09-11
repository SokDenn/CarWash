package org.example.service;

import org.example.Jwt.JwtRequest;
import org.example.Jwt.JwtResponse;
import org.example.Jwt.JwtUtil;
import org.example.model.User;
import org.example.security.ActionException;
import org.example.security.ActionResponse;
import org.example.security.SecurityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

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

    public ActionResponse deleteAccount() {
        User currentUser = userService.getAuthenticationUser();
        String currentUserRoleStr = currentUser.getRole().getName();

        if (currentUserRoleStr.equals("ADMIN") || currentUserRoleStr.equals("OPERATOR")) {
            throw new ActionException("Удалить свой аккаунт может только пользователь!");

        }

        // Выполняем удаление пользователя и отмену бронирований
        userService.deleteUser(currentUser.getId());
        reservationStatusService.cancelledReservationFromUser(currentUser.getId());

        return new ActionResponse(
                "redirect:/logout",
                "Аккаунт удален!"
        );
    }

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

    public void passwordRecovery(String username){
        String token = jwtUtil.generatePasswordResetToken(username);

        String resetLink = "http://localhost:8080/api/v1/auth/passwordReplacement?token=" + token;
        System.out.println("Ссылка для восстановления пароля: " + resetLink);
    }

    public boolean isValidToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return username != null && jwtUtil.validateToken(token, userService.loadUserByUsername(username));
    }

    public User getUserFromToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return userService.findByUsername(username);
    }

    public ActionResponse updatePassword(String token, String newPassword) {
        ActionResponse actionResponse = new ActionResponse();

        if (isValidToken(token)) {
            String username = getUserFromToken(token).getUsername();
            userService.updatePassword(username, newPassword);

            actionResponse.setMessage("Ваш пароль был успешно изменен.");

        } else {
            actionResponse.setMessage("Недействительная или истекшая ссылка для сброса пароля.");
        }

        return actionResponse;
    }
}
