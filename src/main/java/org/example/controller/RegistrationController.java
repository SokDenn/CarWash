package org.example.controller;

import org.example.model.User;
import org.example.repo.UserRepo;
import org.example.security.SecurityValidator;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registration")
public class RegistrationController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityValidator securityValidator;

    @GetMapping
    public String registration(RedirectAttributes redirectAttributes){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (securityValidator.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("message",
                    "Вы уже зарегистрированы! Необходимо сначала выйти из системы");

            return "redirect:/api/reservations";
        }
        return "registration";
    }

    @PostMapping
    public String addUser(User user, Model model) {

        userService.createUser(user.getUsername(), user.getPassword(), null);

        return "redirect:/login";
    }
}
