package org.example.controller;

import org.example.dto.ReservationListViewDTO;
import org.example.service.BoxService;
import org.example.service.ReservationService;
import org.example.service.ReservationStatusService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/**
 * Контроллер для отображения и удаления бронирований.
 */
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

    /**
     * Показать отфильтрованные бронирования
     * или доход за период(может получить только админ).
     *
     * @param boxIdFilter фильтр по идентификатору бокса
     * @param startDateTimeStr начальная дата фильтра
     * @param endDateTimeStr конечная дата фильтра
     * @param activeReservations фильтр активных бронирований
     * @param displayRevenue флаг отображения выручки
     * @param model объект для передачи данных на страницу
     * @return страница с бронированиями
     */
    @GetMapping
    public String reservations(@RequestParam(name = "boxIdFilter", required = false) UUID boxIdFilter,
                               @RequestParam(name = "startDateTimeFilter", required = false) String startDateTimeStr,
                               @RequestParam(name = "endDateTimeFilter", required = false) String endDateTimeStr,
                               @RequestParam(name = "activeReservations", required = false) Boolean activeReservations,
                               @RequestParam(name = "displayRevenue", required = false) Boolean displayRevenue,
                               Model model) {

        ReservationListViewDTO reservationListViewDTO = reservationService.getFilteredReservations(
                boxIdFilter, startDateTimeStr, endDateTimeStr, activeReservations, displayRevenue);

        model.addAttribute("message", reservationListViewDTO.getMessage());
        model.addAttribute("reservationDTO", reservationListViewDTO);

        return "reservations";
    }

    /**
     * Удалить бронирование (доступно для ADMIN).
     *
     * @param id идентификатор бронирования
     * @return перенаправление на страницу с бронированиями
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteReservation(@PathVariable UUID id) {

        reservationService.deleteReservation(id);

        return "redirect:/api/reservations";
    }
}
