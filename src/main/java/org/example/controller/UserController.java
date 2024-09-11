package org.example.controller;

import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

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

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getAllUsers(Model model) {

        model.addAttribute("userList", userService.findAllActive());
        model.addAttribute("discount", discountService.findDiscount());
        model.addAttribute("userAuthentication", userService.getAuthenticationUser());

        return "users";
    }
    @GetMapping("/checkUsername")
    public ResponseEntity<Map<String, Boolean>> checkUsername(
                                    @RequestParam String username,
                                    @RequestParam(required = false) UUID userId) {

        boolean unique = userService.isUsernameUnique(userId, username);
        return ResponseEntity.ok(Collections.singletonMap("unique", unique));
    }

    @GetMapping({"editUser/{userId}", "/editUser"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editUser(@PathVariable(name = "userId", required = false) UUID userId,
                          Model model) {

        if (userId != null) {
            model.addAttribute("user", userService.getUserById(userId));
        }
        model.addAttribute("roleList", roleService.findAll());
        model.addAttribute("userList", userService.findAllActive());

        return "editUser";
    }

    @PostMapping("/editUser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("roleId") UUID roleId) {

        userService.createUser(username, password, roleId);
        return "redirect:/api/users";
    }

    @PostMapping("editUser/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String  updateWashing(@PathVariable("userId") UUID userId,
                                 @RequestParam("username") String username,
                                 @RequestParam("password") String password,
                                 @RequestParam("roleId") UUID roleId) {

        userService.updateUser(userId, username, password, roleId);

        return "redirect:/api/users";
    }

    @PostMapping("/discount")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateDiscountOperator(@RequestParam("minDiscount") Integer minDiscount,
                                         @RequestParam("maxDiscount") Integer maxDiscount) {

        discountService.saveDiscount(minDiscount, maxDiscount);
        return "redirect:/api/users";
    }

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
