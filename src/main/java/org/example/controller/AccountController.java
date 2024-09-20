package org.example.controller;

import org.example.model.User;
import org.example.service.AccountService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Контроллер для управления действиями пользователя с его аккаунтом.
 */
@Controller
@RequestMapping("/api/user/account")
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;


    /**
     * Показать страницу редактирования аккаунта текущего пользователя.
     *
     * @param model объект Model для передачи данных на страницу
     * @return возвращает страницу "editAccount"
     */
    @GetMapping("/edit")
    public String editAccount(Model model) {
        User currentUser = userService.getAuthenticationUser();

        model.addAttribute("user", currentUser);

        return "editAccount";
    }

    /**
     * Обновить данные аккаунта пользователя.
     *
     * @param username новое имя пользователя
     * @param password новый пароль
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу редактирования аккаунта с сообщением об успехе
     */
    @PostMapping("/edit")
    public String updateAccount(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                RedirectAttributes redirectAttributes) {

        UUID currentUserId = userService.getAuthenticationUser().getId();
        userService.updateUser(currentUserId, username, password, null);

        redirectAttributes.addFlashAttribute("message", "Аккаунт обновлен!");

        return "redirect:/api/user/account/edit";
    }

    /**
     * Удалить аккаунт пользователя.
     *
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу выхода или редактирования аккаунта в зависимости от результата удаления
     */
    @DeleteMapping("/delete")
    public String deleteAccount(RedirectAttributes redirectAttributes) {

        String message = accountService.deleteAccount();
        redirectAttributes.addFlashAttribute("message", message);

        if (message.equals("Аккаунт удален!")) {
            return "redirect:/logout";
        }

        return "redirect:/api/user/account/edit";
    }
}
