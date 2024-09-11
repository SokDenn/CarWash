package org.example.controller;

import org.example.Jwt.JwtRequest;
import org.example.Jwt.JwtResponse;
import org.example.Jwt.JwtUtil;
import org.example.model.User;
import org.example.security.ActionResponse;
import org.example.service.AccountService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            JwtResponse jwtResponse = accountService.authenticateUser(authenticationRequest);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtResponse.getAccessTokenCookie().toString())
                    .body(jwtResponse);

        } catch (RuntimeException e) {
            if (e.getMessage().equals("Некорректный логин или пароль")) {
                return ResponseEntity.status(401).body(e.getMessage());

            } else {
                return ResponseEntity.status(500).body("{\"ошибка\":\"Ошибка сервера\"}");
            }
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRequest refreshRequest) throws Exception {
        try {
            JwtResponse jwtResponse = accountService.updateAuthenticateUser(refreshRequest);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtResponse.getAccessTokenCookie().toString())
                    .body(jwtResponse);

        } catch (RuntimeException e) {
            if (e.getMessage().equals("Некорректный refresh token")) {
                return ResponseEntity.status(403).body(e.getMessage());

            } else {
                return ResponseEntity.status(500).body("{\"ошибка\":\"Ошибка сервера\"}");
            }
        }
    }

    @GetMapping("/passwordRecovery")
    public String getPasswordRecovery(RedirectAttributes redirectAttributes) {
        return "passwordRecovery";
    }


    @PostMapping("/passwordRecovery")
    public String passwordRecovery(@RequestParam("username") String username, RedirectAttributes redirectAttributes) {

        accountService.passwordRecovery(username);

        redirectAttributes.addFlashAttribute("message",
                "Инструкции по восстановлению пароля отправлены на ваш email.");

        return "redirect:/login";
    }

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

    @PostMapping("/passwordReplacement")
    public String passwordReplacement(@RequestParam("token") String token,
                                      @RequestParam("password") String newPassword,
                                      RedirectAttributes redirectAttributes) {

        ActionResponse actionResponse = accountService.updatePassword(token, newPassword);
        redirectAttributes.addFlashAttribute("message", actionResponse.getMessage());

        return "redirect:/login";
    }
}
