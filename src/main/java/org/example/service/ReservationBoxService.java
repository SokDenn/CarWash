package org.example.service;

import org.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationBoxService {
    @Autowired
    private BoxService boxService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserService userService;
    @Autowired
    private WashingService washingService;

    public Box findSuitableBox(Washing washing, LocalDateTime startDateTime, Reservation currentReservation) {
        List<Box> availableBoxes = boxService.findAllActive();

        // Фильтруем боксы по времени открытия, закрытия и наличию пересекающихся бронирований
        List<Box> suitableBoxes = availableBoxes.stream()
                .filter(box -> isBoxAvailable(box, startDateTime, washing.getDurationMinute(), currentReservation))
                .sorted(Comparator.comparing(Box::getWashingСoefficient))
                .toList();

        return suitableBoxes.isEmpty() ? null : suitableBoxes.get(0);
    }

    private boolean isBoxAvailable(Box box, LocalDateTime startDateTime, int durationMinutes, Reservation currentReservation) {
        LocalDateTime endDateTime = startDateTime.plusMinutes
                ((long) (durationMinutes * box.getWashingСoefficient().floatValue()));

        if (startDateTime.toLocalTime().isBefore(box.getOpeningTime()) ||
                endDateTime.toLocalTime().isAfter(box.getClosingTime())) {
            return false;
        }

        List<Reservation> reservations = reservationService.findRelevantReservationsByBox
                (box.getId(), startDateTime.minusHours(1), endDateTime.plusHours(1));

        // Проверяем на пересечения с бронированиями, исключая текущее бронирование
        for (Reservation reservation : reservations) {
            if (!reservation.equals(currentReservation) &&
                    reservation.getEndDateTime().isAfter(startDateTime) &&
                    reservation.getStartDateTime().isBefore(endDateTime)) {
                return false;
            }
        }

        return true;
    }

    public Reservation createReservation(UUID washingId, LocalDateTime startDateTime) {
        Washing washing = washingService.getWashingById(washingId);
        if (washing == null) throw new IllegalArgumentException("Тип мойки не найден");

        Box suitableBox = findSuitableBox(washing, startDateTime, null);
        if (suitableBox == null) {
            return null;
        }

        User user = userService.getAuthenticationUser();
        Reservation reservation = new Reservation(
                suitableBox,
                startDateTime,
                startDateTime.plusMinutes((long) (washing.getDurationMinute() * suitableBox.getWashingСoefficient().floatValue())),
                washing,
                user
        );

        Reservation reservationSave = reservationService.saveReservation(reservation);
        String confirmationLink = "http://localhost:8080/api/reservations/confirm/" + reservation.getId();
        System.out.println("Для подтверждения брони перейдите по ссылке: " + confirmationLink);

        return reservationSave;
    }

    public boolean updateReservation(UUID reservationId, UUID washingId, LocalDateTime startDateTime) {
        Washing washing = washingService.getWashingById(washingId);
        if (washing == null) {
            throw new IllegalArgumentException("Тип мойки не найден");
        }

        Reservation editReservation = reservationService.getReservationById(reservationId);
        if (isUnchangedOrCompleted(editReservation, washingId, startDateTime)) {
            return false;
        }

        Box suitableBox = findSuitableBox(washing, startDateTime, editReservation);
        if (suitableBox == null) {
            return false;
        }

        updateReservationDetails(editReservation, washing, suitableBox, startDateTime);
        reservationService.saveReservation(editReservation);

        return true;
    }

    private boolean isUnchangedOrCompleted(Reservation reservation, UUID washingId, LocalDateTime startDateTime) {
        return reservation.getWashing().getId().equals(washingId)
                && reservation.getStartDateTime().equals(startDateTime)
                || reservation.getStatus().equals(Status.COMPLETED.toString())
                || reservation.getStatus().equals(Status.CANCELLED.toString());
    }

    private void updateReservationDetails(Reservation reservation, Washing washing, Box box, LocalDateTime startDateTime) {
        reservation.setBox(box);
        reservation.setWashing(washing);
        reservation.setDiscount(0);
        reservation.setResultPrice(washing.getPrice());
        reservation.setStatus(Status.WAITING_RESERVATION.toString());
        reservation.setStartDateTime(startDateTime);
        reservation.setEndDateTime(calculateEndDateTime(startDateTime, washing, box));
    }

    private LocalDateTime calculateEndDateTime(LocalDateTime startDateTime, Washing washing, Box box) {
        return startDateTime.plusMinutes((long)
                (washing.getDurationMinute() * box.getWashingСoefficient().floatValue()));
    }
}
