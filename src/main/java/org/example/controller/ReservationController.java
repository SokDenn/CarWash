package org.example.controller;

import org.example.model.Reservation;
import org.example.model.User;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    ReservationService reservationService;
    @Autowired
    UserService userService;
    @Autowired
    BoxService boxService;
    @Autowired
    ReservationStatusService reservationStatusService;

    @GetMapping
    public String reservations(@RequestParam(name = "boxIdFilter", required = false) UUID boxIdFilter,
                               @RequestParam(name = "startDateTimeFilter", required = false) String startDateTimeStr,
                               @RequestParam(name = "endDateTimeFilter", required = false) String endDateTimeStr,
                               @RequestParam(name = "activeReservations", required = false) Boolean activeReservations,
                               @RequestParam(name = "displayRevenue", required = false) Boolean displayRevenue,
                               Model model) {

        List<Reservation> reservationList = reservationService.getFilteredReservations(
                boxIdFilter, startDateTimeStr, endDateTimeStr, activeReservations, displayRevenue);

        if (displayRevenue != null && userService.isAdmin()) {
            model.addAttribute("resultRevenue", reservationService.calculateRevenue(reservationList));
        } else if (displayRevenue != null && !userService.isAdmin()) {
            model.addAttribute("message", "Функция доступна только админу!");
        }

        model.addAttribute("activeReservations", activeReservations);
        model.addAttribute("boxIdFilter", boxIdFilter);
        model.addAttribute("startDateTimeFilter", startDateTimeStr);
        model.addAttribute("endDateTimeFilter", endDateTimeStr);
        model.addAttribute("userAuthentication", userService.getAuthenticationUser());
        model.addAttribute("boxList", boxService.findAllActive());
        model.addAttribute("reservationList", reservationList);

        return "reservations";
    }

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteReservation(@PathVariable UUID id) {

        reservationService.deleteReservation(id);

        return "redirect:/api/reservations";
    }
}
