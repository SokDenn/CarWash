package org.example.controller;

import org.example.service.UserService;
import org.example.service.WashingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления услугами мойки (доступно только для ADMIN).
 */
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/washings")
public class WashingController {
    @Autowired
    private WashingService washingService;
    @Autowired
    private UserService userService;


    /**
     * Показать все услуги мойки.
     *
     * @param message сообщение (опционально)
     * @param model объект для передачи данных на страницу
     * @return страница с услугами мойки
     */
    @GetMapping
    public String getAllWashing(@RequestParam(name = "message", required = false) String message,
                                Model model) {

        model.addAttribute("washingList", washingService.getAllWashing());
        model.addAttribute("userAuthentication", userService.getAuthenticationUser());

        return "washings";
    }

    /**
     * Показать форму для создания или редактирования услуги мойки.
     *
     * @param washingId идентификатор услуги (опционально)
     * @param model объект для передачи данных на страницу
     * @return страницу редактирования услуги
     */
    @GetMapping({"editWashing/{washingId}", "/editWashing"})
    public String editWashing(@PathVariable(name = "washingId", required = false) UUID washingId,
                          Model model) {

        if (washingId != null) {
            model.addAttribute("washing", washingService.getWashingById(washingId));
        }
        return "editWashing";
    }

    /**
     * Создать новую услугу мойки.
     *
     * @param name название услуги
     * @param price цена услуги
     * @param durationMinute продолжительность услуги (в минутах)
     * @return перенаправление на страницу с услугами мойки
     */
    @PostMapping("/editWashing")
    public String createWashing(@RequestParam("name") String name,
                                @RequestParam("price") Integer price,
                                @RequestParam("durationMinute") Integer durationMinute) {

        washingService.createWashing(name, price, durationMinute);

        return "redirect:/api/washings";
    }

    /**
     * Обновить существующую услугу мойки.
     *
     * @param washingId идентификатор услуги
     * @param name название услуги
     * @param price цена услуги
     * @param durationMinute продолжительность услуги (в минутах)
     * @return перенаправление на страницу с услугами мойки
     */
    @PostMapping("editWashing/{washingId}")
    public String updateWashing(@PathVariable("washingId") UUID washingId,
                                @RequestParam("name") String name,
                                @RequestParam("price") Integer price,
                                @RequestParam("durationMinute") Integer durationMinute) {

        washingService.updateWashing(washingId, name, price, durationMinute);

        return "redirect:/api/washings";
    }

    /**
     * Удалить услугу мойки.
     *
     * @param id идентификатор услуги
     * @return перенаправление на страницу с услугами мойки
     */
    @DeleteMapping("/{id}")
    public String deleteWashing(@PathVariable UUID id) {
        washingService.deleteWashing(id);
        return "redirect:/api/washings";
    }
}
