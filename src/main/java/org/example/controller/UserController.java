package org.example.controller;

import org.example.dto.UserDTO;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для управления пользователями.
 */
@Controller
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    ReservationStatusService reservationStatusService;
    @Autowired
    AccountService accountService;

    /**
     * Показать всех пользователей (только для ADMIN).
     *
     * @param model объект для передачи данных на страницу
     * @return страницу с пользователями
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getAllUsers(Model model) {

        UserDTO userDTO = accountService.getAllUsers();
        model.addAttribute("userDTO", userDTO);

        return "users";
    }

    /**
     * Проверить уникальность имени пользователя.
     *
     * @param username имя пользователя
     * @param userId идентификатор пользователя (опционально)
     * @return результат проверки уникальности
     */
    @GetMapping("/checkUsername")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
            @RequestParam String username,
            @RequestParam(required = false) UUID userId) {

        boolean unique = userService.isUsernameUnique(userId, username);
        return ResponseEntity.ok(Collections.singletonMap("unique", unique));
    }

    /**
     * Показать форму редактирования пользователя (только для ADMIN).
     *
     * @param userId идентификатор пользователя (опционально)
     * @param model объект для передачи данных на страницу
     * @return страницу редактирования пользователя
     */
    @GetMapping({"editUser/{userId}", "/editUser"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editUser(@PathVariable(name = "userId", required = false) UUID userId,
                           Model model) {

        UserDTO userDTO = accountService.getEditUsers(userId);
        model.addAttribute("userDTO", userDTO);

        return "editUser";
    }

    /**
     * Создать нового пользователя (только для ADMIN).
     *
     * @param username имя пользователя
     * @param password пароль
     * @param roleId идентификатор роли
     * @return перенаправление на страницу пользователей
     */
    @PostMapping("/editUser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("roleId") UUID roleId) {

        userService.createUser(username, password, roleId);
        return "redirect:/api/users";
    }

    /**
     * Обновить пользователя (только для ADMIN).
     *
     * @param userId идентификатор пользователя
     * @param username имя пользователя
     * @param password пароль
     * @param roleId идентификатор роли
     * @return перенаправление на страницу пользователей
     */
    @PostMapping("editUser/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateWashing(@PathVariable("userId") UUID userId,
                                @RequestParam("username") String username,
                                @RequestParam("password") String password,
                                @RequestParam("roleId") UUID roleId) {

        userService.updateUser(userId, username, password, roleId);

        return "redirect:/api/users";
    }

    /**
     * Обновить минимальную и максимальную скидку оператора (только для ADMIN).
     *
     * @param minDiscount минимальная скидка
     * @param maxDiscount максимальная скидка
     * @return перенаправление на страницу пользователей
     */
    @PostMapping("/discount")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateDiscountOperator(@RequestParam("minDiscount") Integer minDiscount,
                                         @RequestParam("maxDiscount") Integer maxDiscount) {

        discountService.saveDiscount(minDiscount, maxDiscount);
        return "redirect:/api/users";
    }

    /**
     * Удалить пользователя (только для ADMIN).
     *
     * @param id идентификатор пользователя
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу пользователей
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteUser(@PathVariable UUID id,
                             RedirectAttributes redirectAttributes) {
        UUID currentUserId = userService.getAuthenticationUser().getId();

        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("message",
                    "Администратор не может удалить свой собственный аккаунт.");
            return "redirect:/api/users";
        }

        reservationStatusService.cancelledReservationFromUser(id);
        return "redirect:/api/users";
    }
}
