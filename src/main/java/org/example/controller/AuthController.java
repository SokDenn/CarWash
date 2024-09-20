package org.example.controller;

import org.example.Jwt.JwtRequest;
import org.example.Jwt.JwtResponse;
import org.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для аутентификации пользователей и восстановления пароля.
 */
@Controller
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AccountService accountService;

    /**
     * Аутентифицировать пользователя и вернуть JWT токен.
     *
     * @param authenticationRequest данные для аутентификации (логин и пароль)
     * @return объект ResponseEntity с JWT токеном или сообщением об ошибке
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {

        JwtResponse jwtResponse = new JwtResponse();
        try {
            jwtResponse = accountService.authenticateUser(authenticationRequest);

        } catch (RuntimeException e) {
            if (e.getMessage().equals("Некорректный логин или пароль")) {
                return ResponseEntity.status(401).body(e.getMessage());
            } else {
                return ResponseEntity.status(500).body("{\"ошибка\":\"Ошибка сервера\"}");
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtResponse.getAccessTokenCookie().toString())
                .body(jwtResponse);
    }

    /**
     * Обновить JWT токен по refresh токену.
     *
     * @param refreshRequest данные для обновления токена
     * @return объект ResponseEntity с новым JWT токеном или сообщением об ошибке
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRequest refreshRequest) throws Exception {

        JwtResponse jwtResponse = new JwtResponse();
        try {
            jwtResponse = accountService.updateAuthenticateUser(refreshRequest);

        } catch (RuntimeException e) {
            if (e.getMessage().equals("Некорректный refresh token")) {
                return ResponseEntity.status(403).body(e.getMessage());
            } else {
                return ResponseEntity.status(500).body("{\"ошибка\":\"Ошибка сервера\"}");
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtResponse.getAccessTokenCookie().toString())
                .body(jwtResponse);
    }

    /**
     * Показать страницу восстановления пароля.
     *
     * @return страница восстановления пароля
     */
    @GetMapping("/passwordRecovery")
    public String getPasswordRecovery(RedirectAttributes redirectAttributes) {
        return "passwordRecovery";
    }

    /**
     * Отправить инструкции по восстановлению пароля на email.
     *
     * @param username имя пользователя для восстановления пароля
     * @return перенаправление на страницу логина с сообщением об отправке инструкций
     */
    @PostMapping("/passwordRecovery")
    public String passwordRecovery(@RequestParam("username") String username, RedirectAttributes redirectAttributes) {

        accountService.passwordRecovery(username);

        redirectAttributes.addFlashAttribute("message",
                "Инструкции по восстановлению пароля отправлены на ваш email.");

        return "redirect:/login";
    }

    /**
     * Показать страницу для замены пароля по токену восстановления.
     *
     * @param token токен восстановления пароля
     * @param model объект Model для передачи данных на страницу
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return страница замены пароля или перенаправление на страницу логина
     */
    @GetMapping("/passwordReplacement")
    public String getPasswordReplacement(@RequestParam("token") String token,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {

        if (accountService.isValidToken(token)) {
            model.addAttribute("token", token);
            model.addAttribute("user", accountService.getUserFromToken(token));
            return "passwordReplacement";

        } else {
            redirectAttributes.addFlashAttribute("message",
                    "Недействительная или истекшая ссылка для сброса пароля.");
            return "redirect:/login";
        }
    }

    /**
     * Заменить старый пароль новым.
     *
     * @param token токен восстановления пароля
     * @param newPassword новый пароль
     * @return перенаправление на страницу логина с сообщением об успехе
     */
    @PostMapping("/passwordReplacement")
    public String passwordReplacement(@RequestParam("token") String token,
                                      @RequestParam("password") String newPassword,
                                      RedirectAttributes redirectAttributes) {

        String message = accountService.updatePassword(token, newPassword);
        redirectAttributes.addFlashAttribute("message", message);

        return "redirect:/login";
    }
}
