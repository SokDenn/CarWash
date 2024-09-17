package org.example.controller;

import org.example.service.BoxService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/boxes")
public class BoxController {
    @Autowired
    private BoxService boxService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String boxes(Model model) {

        model.addAttribute("userAuthentication", userService.getAuthenticationUser());
        model.addAttribute("boxList", boxService.findAllActive());

        return "boxes";
    }

    @GetMapping({"editBox/{boxId}", "/editBox"})
    public String editBox(@PathVariable(name = "boxId", required = false) UUID boxId,
                          Model model) {

        if (boxId != null) {
            model.addAttribute("box", boxService.getBoxById(boxId));
        }
        model.addAttribute("boxNumberList", boxService.getAvailableBoxNumbers(boxId));
        model.addAttribute("userOperatorList", userService.getAllUsersOperator());

        return "editBox";
    }

    @PostMapping("/editBox")
    public String createBox(@RequestParam("boxNumber") Integer boxNumber,
                            @RequestParam(name = "userOperatorId", required = false) UUID userOperatorId,
                            @RequestParam("washingСoefficient") BigDecimal washingСoefficient,
                            @RequestParam("openingTime") String openingTimeStr,
                            @RequestParam("closingTime") String closingTimeStr) {

        boxService.createBox(boxNumber, userOperatorId, washingСoefficient, openingTimeStr, closingTimeStr);

        return "redirect:/api/boxes";
    }

    @PostMapping("editBox/{boxId}")
    public String updateBox(@PathVariable("boxId") UUID boxId,
                            @RequestParam("boxNumber") Integer boxNumber,
                            @RequestParam(name = "userOperatorId", required = false) UUID userOperatorId,
                            @RequestParam("washingСoefficient") BigDecimal washingСoefficient,
                            @RequestParam("openingTime") String openingTimeStr,
                            @RequestParam("closingTime") String closingTimeStr) {

        boxService.updateBox(boxId, boxNumber, userOperatorId, washingСoefficient, openingTimeStr, closingTimeStr);

        return "redirect:/api/boxes";
    }

    @DeleteMapping("/{boxId}")
    public String deleteBox(@PathVariable UUID boxId) {
        boxService.deleteBox(boxId);

        return "redirect:/api/boxes";
    }
}
