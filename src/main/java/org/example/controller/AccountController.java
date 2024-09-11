package org.example.controller;

import org.example.security.ActionResponse;
import org.example.model.User;
import org.example.service.AccountService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/api/user/account")
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;


    @GetMapping("/edit")
    public String editAccount(Model model) {
        User currentUser = userService.getAuthenticationUser();

        model.addAttribute("user", currentUser);

        return "editAccount";
    }

    @PostMapping("/edit")
    public String updateAccount(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                RedirectAttributes redirectAttributes) {

        UUID currentUserId = userService.getAuthenticationUser().getId();
        userService.updateUser(currentUserId, username, password, null);

        redirectAttributes.addFlashAttribute("message",
                "Аккаунт обновлен!");

        return "redirect:/api/user/account/edit";
    }

    @DeleteMapping("/delete")
    public String deleteAccount(RedirectAttributes redirectAttributes) {

        ActionResponse actionResponse = accountService.deleteAccount();
        redirectAttributes.addFlashAttribute("message", actionResponse.getMessage());

        return actionResponse.getUrl();
    }
}
