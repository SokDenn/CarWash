package org.example.service;

import org.example.model.Reservation;
import org.example.model.Status;
import org.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReservationStatusService {
    @Autowired
    private UserService userService;
    @Autowired
    private ReservationService reservationService;

    @Scheduled(fixedRate = 60000) // Проверять каждую минуту
    public void checkReservations() {
        LocalDateTime currentTime = LocalDateTime.now();

        // Проверка на истечение 15 минут для статуса WAITING_RESERVATION
        reservationService.findByStatus(Status.WAITING_RESERVATION.toString()).forEach(reservation -> {
            Duration duration = Duration.between(reservation.getCreationDateTime(), currentTime);
            if (duration.toMinutes() > 15) {
                reservation.setStatus(Status.CANCELLED.toString());
                reservationService.saveReservation(reservation);
            }
        });

        // Проверка на истечение времени для статуса BOOKED
        reservationService.findByStatus(Status.BOOKED.toString()).forEach(reservation -> {
            if (currentTime.isAfter(reservation.getStartDateTime())) {
                reservation.setStatus(Status.CANCELLED.toString());
                reservationService.saveReservation(reservation);
            }
        });

        // Автоматическое завершение мойки
        reservationService.findByStatus(Status.AT_CAR_WASH.toString()).forEach(reservation -> {
            if (currentTime.isAfter(reservation.getEndDateTime())) {
                reservation.setStatus(Status.COMPLETED.toString());
                reservationService.saveReservation(reservation);
            }
        });
    }


    public Map<String, String> getStatusChangeButtons(UUID reservationId) {
        User user = userService.getAuthenticationUser();
        Reservation reservation = reservationService.getReservationById(reservationId);

        Map<String, String> buttons = new HashMap<>();

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startDateTime = reservation.getStartDateTime();
        Duration timeUntilStart = Duration.between(currentTime, startDateTime);

        if (reservation.getStatus().equals(Status.BOOKED.toString())
                && (reservation.getUser().getId() == userService.getAuthenticationUser().getId()
                || user.getRole().getName().equals("ADMIN"))
                && timeUntilStart.toMinutes() <= 60
                && timeUntilStart.toMinutes() >= 0) {

            buttons.put("На автомойке", Status.AT_CAR_WASH.toString());
        }

        if (reservation.getStatus().equals(Status.WAITING_RESERVATION.toString())
                || reservation.getStatus().equals(Status.BOOKED.toString())
                || reservation.getStatus().equals(Status.AT_CAR_WASH.toString())) {

            buttons.put("Отменить", Status.CANCELLED.toString());
        }

        if (reservation.getStatus().equals(Status.AT_CAR_WASH.toString())
                && (user.getRole().getName().equals("OPERATOR") || user.getRole().getName().equals("ADMIN"))
                && currentTime.isAfter(startDateTime)) {

            buttons.put("Завершить", Status.COMPLETED.toString());
        }

        return buttons;
    }

    public boolean confirmReservation(UUID reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation != null && reservation.getStatus().equals(Status.WAITING_RESERVATION.toString())) {
            reservation.setStatus(Status.BOOKED.toString());
            reservationService.saveReservation(reservation);
            return true;
        }
        return false;
    }

    public void cancelledReservationFromUser(UUID userId) {
        List<Reservation> reservationList = reservationService.findByUserId(userId);

        if (reservationList != null) {
            List<Reservation> reservationListActive = reservationList.stream().filter(
                    reservation -> reservation.getStatus().equals(Status.WAITING_RESERVATION.toString())
                            || reservation.getStatus().equals(Status.BOOKED.toString())).toList();

            for (Reservation reservation : reservationListActive) {
                reservation.setStatus(Status.CANCELLED.toString());
                reservationService.saveReservation(reservation);
            }
        }
    }

    public boolean updateReservationStatus(UUID reservationId, String newStatusStr) {

        Reservation reservation = reservationService.getReservationById(reservationId);
        Status newStatus = Status.nameEntityOfStatus(newStatusStr);

        if (reservation != null && newStatus != null) {
            reservation.setStatus(newStatusStr);

            if (newStatus.name().equals("Завершено")) {
                reservation.setEndDateTime(LocalDateTime.now().withSecond(0).withNano(0));
            }

            reservationService.saveReservation(reservation);
            return true;
        }
        return false;
    }


}
