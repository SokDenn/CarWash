package org.example.controller;

import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.ReservationStatusService;
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
    private ReservationService reservationService;
    @Autowired
    private ReservationStatusService reservationStatusService;

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

    @DeleteMapping("/delete") public String deleteAccount(RedirectAttributes redirectAttributes) {

        User currentUser = userService.getAuthenticationUser();
        String currentUserRoleStr = currentUser.getRole().getName();

        if (currentUserRoleStr.equals("ADMIN") || currentUserRoleStr.equals("OPERATOR")) {
            redirectAttributes.addFlashAttribute("message",
                    "Удалить свой аккаунт может только пользователь!");

            return "redirect:/api/user/account/edit";
        }

        userService.deleteUser(currentUser.getId());
        reservationStatusService.cancelledReservationFromUser((currentUser.getId()));
        return "redirect:/logout";
    }
}
