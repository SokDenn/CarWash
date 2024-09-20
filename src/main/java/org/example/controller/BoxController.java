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

/**
 * Контроллер для управления боксами.
 * Все действия доступны только для пользователей с ролью 'ADMIN'.
 */
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("/api/boxes")
public class BoxController {
    @Autowired
    private BoxService boxService;
    @Autowired
    private UserService userService;

    /**
     * Показать список всех активных боксов.
     *
     * @param model объект Model для передачи данных в представление
     * @return возвращает страницу с боксами
     */
    @GetMapping
    public String boxes(Model model) {

        model.addAttribute("userAuthentication", userService.getAuthenticationUser());
        model.addAttribute("boxList", boxService.findAllActive());

        return "boxes";
    }

    /**
     * Показать страницу редактирования бокса нового или текущего бокса.
     *
     * @param boxId идентификатор бокса для редактирования
     * @param model объект Model для передачи данных в представление
     * @return возвращает страницу редактирования бокса
     */
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

    /**
     * Создать новый бокс.
     *
     * @param boxNumber номер бокса
     * @param userOperatorId идентификатор оператора, закрепленного за боксом
     * @param washingСoefficient коэффициент мойки
     * @param openingTimeStr время открытия бокса
     * @param closingTimeStr время закрытия бокса
     * @return перенаправление на страницу со списком боксов
     */
    @PostMapping("/editBox")
    public String createBox(@RequestParam("boxNumber") Integer boxNumber,
                            @RequestParam(name = "userOperatorId", required = false) UUID userOperatorId,
                            @RequestParam("washingСoefficient") BigDecimal washingСoefficient,
                            @RequestParam("openingTime") String openingTimeStr,
                            @RequestParam("closingTime") String closingTimeStr) {

        boxService.createBox(boxNumber, userOperatorId, washingСoefficient, openingTimeStr, closingTimeStr);

        return "redirect:/api/boxes";
    }

    /**
     * Обновить существующий бокс.
     *
     * @param boxId идентификатор бокса для обновления
     * @param boxNumber новый номер бокса
     * @param userOperatorId идентификатор оператора
     * @param washingСoefficient новый коэффициент мойки
     * @param openingTimeStr новое время открытия
     * @param closingTimeStr новое время закрытия
     * @return перенаправление на страницу со списком боксов
     */
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

    /**
     * Удалить бокс по его идентификатору.
     *
     * @param boxId идентификатор бокса для удаления
     * @return перенаправление на страницу со списком боксов
     */
    @DeleteMapping("/{boxId}")
    public String deleteBox(@PathVariable UUID boxId) {
        boxService.deleteBox(boxId);

        return "redirect:/api/boxes";
    }
}
