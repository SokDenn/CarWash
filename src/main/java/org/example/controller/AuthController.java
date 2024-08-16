package org.example.controller;

import org.example.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.example.Jwt.JwtRequest;
import org.example.Jwt.JwtResponse;
import org.example.Jwt.JwtUtil;
import org.example.security.SecurityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private SecurityValidator securityValidator;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            Authentication authentication = securityValidator.authenticate(authenticationRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails, true);
            String refreshToken = jwtUtil.generateToken(userDetails, false);

            ResponseCookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .body(new JwtResponse(accessToken, refreshToken));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Некорректный логин или пароль");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"ошибка\":\"Ошибка сервера\"}");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestBody JwtRequest refreshRequest) throws Exception {
        String refreshToken = refreshRequest.getToken();

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(refreshToken, userDetails)) {

            String accessToken = jwtUtil.generateToken(userDetails, true);
            ResponseCookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .body(new JwtResponse(accessToken, refreshToken));

        } else {
            return ResponseEntity.status(403).body("Некорректный refresh token");
        }
    }

    @GetMapping("/passwordRecovery")
    public String getPasswordRecovery(RedirectAttributes redirectAttributes) {

        return "passwordRecovery";
    }


    @PostMapping("/passwordRecovery")
    public String passwordRecovery(@RequestParam("username") String username, RedirectAttributes redirectAttributes) {

        String token = jwtUtil.generatePasswordResetToken(username);

        String resetLink = "http://localhost:8080/api/v1/auth/passwordReplacement?token=" + token;
        System.out.println("Ссылка для восстановления пароля: " + resetLink);

        redirectAttributes.addFlashAttribute("message",
                "Инструкции по восстановлению пароля отправлены на ваш email.");

        return "redirect:/login";
    }

    @GetMapping("/passwordReplacement")
    public String getPasswordReplacement(@RequestParam("token") String token,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {

        try {
            String username = jwtUtil.getUsernameFromToken(token);

            if (username != null && jwtUtil.validateToken(token, userDetailsService.loadUserByUsername(username))) {
                model.addAttribute("token", token);
                model.addAttribute("user", userService.findByUsername(username));
                return "passwordReplacement";

            } else {
                redirectAttributes.addFlashAttribute("message",
                        "Недействительная или истекшая ссылка для сброса пароля.");
                return "redirect:/login";
            }

        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ссылка устарела");
            return "redirect:/login";
        }

    }

    @PostMapping("/passwordReplacement")
    public String passwordReplacement(@RequestParam("token") String token,
                                      @RequestParam("password") String newPassword,
                                      RedirectAttributes redirectAttributes) {

        String username = jwtUtil.getUsernameFromToken(token);

        if (username != null && jwtUtil.validateToken(token, userDetailsService.loadUserByUsername(username))) {

            userService.updatePassword(username, newPassword);
            redirectAttributes.addFlashAttribute("message", "Ваш пароль был успешно изменен.");
            return "redirect:/login";

        } else {
            redirectAttributes.addFlashAttribute("message",
                    "Недействительная или истекшая ссылка для сброса пароля.");
            return "redirect:/login";
        }
    }
}
