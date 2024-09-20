package org.example.controller;

import org.example.dto.ReservationDTO;
import org.example.security.SecurityValidator;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Контроллер для создания и редактирования бронирований.
 */
@Controller
@RequestMapping("/api/reservations/editReservation")
public class EditReservationController {
    @Autowired
    private WashingService washingService;
    @Autowired
    private ReservationBoxService reservationBoxService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationStatusService reservationStatusService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private SecurityValidator securityValidator;

    /**
     * Показать форму для создания нового бронирования.
     *
     * @param model объект Model для передачи данных в представление
     * @return возвращает страницу создания брони
     */
    @GetMapping
    public String createReservation(Model model) {

        ReservationDTO reservationDTO = securityValidator.canEditReservation(null);
        model.addAttribute("reservationDTO", reservationDTO);

        return "editReservation";
    }

    /**
     * Создать новое бронирование.
     *
     * @param washingId идентификатор мойки
     * @param startDateTime время начала бронирования
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу редактирования бронирования или повторное создание бронирования
     */
    @PostMapping
    public String createReservation(@RequestParam UUID washingId,
                                    @RequestParam LocalDateTime startDateTime,
                                    RedirectAttributes redirectAttributes) {

        ReservationDTO reservationDTO = reservationBoxService.createReservation(washingId, startDateTime);
        redirectAttributes.addFlashAttribute("message", reservationDTO.getMessage());

        if (reservationDTO.getId() == null) {
            return "redirect:/api/reservations/editReservation";
        }
        return "redirect:/api/reservations/editReservation/" + reservationDTO.getId();
    }

    /**
     * Показать форму для редактирования существующего бронирования по его ID.
     *
     * @param reservationId идентификатор бронирования
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @param model объект Model для передачи данных в представление
     * @return возвращает страницу редактирования бронирования
     */
    @GetMapping("/{reservationId}")
    public String editReservationId(@PathVariable UUID reservationId,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        ReservationDTO reservationDTO = securityValidator.canEditReservation(reservationId);

        if (reservationDTO.getId() == null) {
            redirectAttributes.addFlashAttribute("message", reservationDTO.getMessage());
            return "redirect:/api/reservations";
        }
        model.addAttribute("reservationDTO", reservationDTO);
        return "editReservation";
    }

    /**
     * Обновить существующее бронирование.
     *
     * @param reservationId идентификатор бронирования
     * @param washingId идентификатор мойки
     * @param startDateTime новое время начала бронирования
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу редактирования бронирования
     */
    @PostMapping("/{reservationId}")
    public String updateReservation(@PathVariable UUID reservationId,
                                    @RequestParam UUID washingId,
                                    @RequestParam LocalDateTime startDateTime,
                                    RedirectAttributes redirectAttributes) {

        ReservationDTO reservationDTO = reservationBoxService.updateReservation(reservationId, washingId, startDateTime);

        redirectAttributes.addFlashAttribute("message", reservationDTO.getMessage());

        return "redirect:/api/reservations/editReservation/" + reservationId;

    }

    /**
     * Обновить статус бронирования.
     *
     * @param reservationId идентификатор бронирования
     * @param newStatusStr новый статус бронирования
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу редактирования бронирования
     */
    @PostMapping("/{reservationId}/updateStatus")
    public String updateReservationStatus(@PathVariable UUID reservationId,
                                          @RequestParam String newStatusStr,
                                          RedirectAttributes redirectAttributes) {

        reservationStatusService.updateReservationStatus(reservationId, newStatusStr);

        redirectAttributes.addFlashAttribute("message", "Статус брони обновлен");

        return "redirect:/api/reservations/editReservation/" + reservationId;

    }

    /**
     * Подтвердить бронирование.
     *
     * @param reservationId идентификатор бронирования
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу редактирования бронирования
     */
    @GetMapping("/confirm/{reservationId}")
    public String confirmReservation(@PathVariable UUID reservationId,
                                     RedirectAttributes redirectAttributes) {

        boolean result = reservationStatusService.confirmReservation(reservationId);
        if (result) {
            redirectAttributes.addFlashAttribute("message", "Бронь подтверждена.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Бронь не может быть подтверждена.");
        }

        return "redirect:/api/reservations/editReservation/" + reservationId;
    }

    /**
     * Обновить скидку у брони доступно для ролей 'OPERATOR' или 'ADMIN'.
     *
     * @param reservationId идентификатор бронирования
     * @param discount новая скидка
     * @param redirectAttributes объект для передачи сообщений после перенаправления
     * @return перенаправление на страницу редактирования бронирования
     */
    @PreAuthorize("hasAuthority('OPERATOR') or hasAuthority('ADMIN')")
    @PostMapping("/{reservationId}/updateDiscount")
    public String updateReservationDiscount(@PathVariable UUID reservationId,
                                            @RequestParam Integer discount,
                                            RedirectAttributes redirectAttributes) {

        reservationService.updateReservationDiscount(reservationId, discount);

        redirectAttributes.addFlashAttribute("message", "Скидка обновлена");

        return "redirect:/api/reservations/editReservation/" + reservationId;

    }
}
