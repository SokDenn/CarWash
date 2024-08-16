package org.example.controller;

import org.example.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.example.model.Washing;
import org.example.service.WashingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/washings")
public class WashingController {
    @Autowired
    private WashingService washingService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String getAllWashing(@RequestParam(name = "message", required = false) String message,
                                Model model) {

        model.addAttribute("washingList", washingService.getAllWashing());
        model.addAttribute("userAuthentication", userService.getAuthenticationUser());

        return "washings";
    }

    @GetMapping({"editWashing/{washingId}", "/editWashing"})
    public String editBox(@PathVariable(name = "washingId", required = false) UUID washingId,
                          Model model) {

        if (washingId != null) {
            model.addAttribute("washing", washingService.getWashingById(washingId));
        }

        return "editWashing";
    }

    @PostMapping("/editWashing")
    public String createWashing(@RequestParam("name") String name,
                                @RequestParam("price") Integer price,
                                @RequestParam("durationMinute") Integer durationMinute) {

        washingService.createWashing(name, price, durationMinute);

        return "redirect:/api/washings";
    }

    @PostMapping("editWashing/{washingId}")
    public String  updateWashing(@PathVariable("washingId") UUID washingId,
                                 @RequestParam("name") String name,
                                 @RequestParam("price") Integer price,
                                 @RequestParam("durationMinute") Integer durationMinute) {

        washingService.updateWashing(washingId, name, price, durationMinute);

        return "redirect:/api/washings";
    }

    @DeleteMapping("/{id}")
    public String deleteWashing(@PathVariable UUID id) {
        washingService.deleteWashing(id);
        return "redirect:/api/washings";
    }
}
