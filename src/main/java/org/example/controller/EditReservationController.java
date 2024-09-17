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

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setWashingList(washingService.getAllWashing());
        model.addAttribute("reservationDTO", reservationDTO);

        return "editReservation";
    }


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

    @GetMapping("/{reservationId}")
    public String editReservation(@PathVariable UUID reservationId,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        ReservationDTO reservationDTO = securityValidator.canEditReservation(reservationService.getReservationById(reservationId), reservationId);

        if (reservationDTO.getId() == null) {
            redirectAttributes.addFlashAttribute("message", reservationDTO.getMessage());
            return "redirect:/api/reservations";
        }
        model.addAttribute("reservationDTO", reservationDTO);
        return "editReservation";
    }

    @PostMapping("/{reservationId}")
    public String updateReservation(@PathVariable UUID reservationId,
                                    @RequestParam UUID washingId,
                                    @RequestParam LocalDateTime startDateTime,
                                    RedirectAttributes redirectAttributes) {

        ReservationDTO reservationDTO = reservationBoxService.updateReservation(reservationId, washingId, startDateTime);

        redirectAttributes.addFlashAttribute("message", reservationDTO.getMessage());

        return "redirect:/api/reservations/editReservation/" + reservationId;

    }

    @PostMapping("/{reservationId}/updateStatus")
    public String updateReservationStatus(@PathVariable UUID reservationId,
                                          @RequestParam String newStatusStr,
                                          RedirectAttributes redirectAttributes) {

        reservationStatusService.updateReservationStatus(reservationId, newStatusStr);

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
