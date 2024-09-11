package org.example.controller;

import org.example.model.Reservation;
import org.example.security.SecurityValidator;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @GetMapping
    public String editReservation(Model model) {

        model.addAttribute("washingList", washingService.getAllWashing());

        return "editReservation";
    }


    @PostMapping
    public String createReservation(@RequestParam UUID washingId,
                                    @RequestParam LocalDateTime startDateTime,
                                    RedirectAttributes redirectAttributes) {

        Reservation reservation = reservationBoxService.createReservation(washingId, startDateTime);

        if (reservation == null) {
            redirectAttributes.addFlashAttribute("selectedStartDateTime", startDateTime);
            redirectAttributes.addFlashAttribute("selectedWashingId", washingId);
            redirectAttributes.addFlashAttribute("message", "На это время нет свободных боксов! Выберите другое");
            return "redirect:/api/reservations/editReservation";
        }

        redirectAttributes.addFlashAttribute("message", "На почту отправлена ссылка для подтверждения");
        return "redirect:/api/reservations/editReservation/" + reservation.getId();
    }

    @GetMapping("/{reservationId}")
    public String editReservation(@PathVariable UUID reservationId,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        if (!securityValidator.canEditReservation(reservationService.getReservationById(reservationId))) {
            redirectAttributes.addFlashAttribute("message", "Вы не можете редактировать не свою запись!");
            return "redirect:/api/reservations";
        }

        model.addAttribute("reservation", reservationService.getReservationById(reservationId));
        model.addAttribute("washingList", washingService.getAllWashing());
        model.addAttribute("discountList", discountService.getPermittedDiscountList());
        model.addAttribute("statusChangeButtons", reservationStatusService.getStatusChangeButtons(reservationId));

        return "editReservation";
    }

    @PostMapping("/{reservationId}")
    public String updateReservation(@PathVariable UUID reservationId,
                                    @RequestParam UUID washingId,
                                    @RequestParam LocalDateTime startDateTime,
                                    RedirectAttributes redirectAttributes) {

        boolean result = reservationBoxService.updateReservation(reservationId, washingId, startDateTime);

        if (!result) {
            redirectAttributes.addFlashAttribute("message", "Нет свободных боксов или изменения не были внесены");
        } else {
            String confirmationLink = "http://localhost:8080/api/reservations/confirm/" + reservationId;
            System.out.println("Для подтверждения брони перейдите по ссылке: " + confirmationLink);

            redirectAttributes.addFlashAttribute("message", "Забронировано новое время. На почту отправлена ссылка для подтверждения");
        }

        return "redirect:/api/reservations/editReservation/" + reservationId;

    }

    @PostMapping("/{reservationId}/updateStatus")
    public String updateReservationStatus(@PathVariable UUID reservationId,
                                          @RequestParam String newStatusStr,
                                          RedirectAttributes redirectAttributes) {

        boolean result = reservationStatusService.updateReservationStatus(reservationId, newStatusStr);

        redirectAttributes.addFlashAttribute("message", "Статус брони обновлен");

        return "redirect:/api/reservations/editReservation/" + reservationId;

    }

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
