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

    public Box findSuitableBox(Washing washing, LocalDateTime startDateTime) {
        List<Box> availableBoxes = boxService.findAllActive();

        // Фильтруем боксы по времени открытия, закрытия и наличию пересекающихся бронирований
        List<Box> suitableBoxes = availableBoxes.stream()
                .filter(box -> isBoxAvailable(box, startDateTime, washing.getDurationMinute()))
                .sorted(Comparator.comparing(Box::getWashingСoefficient))
                .toList();

        return suitableBoxes.isEmpty() ? null : suitableBoxes.get(0);
    }

    private boolean isBoxAvailable(Box box, LocalDateTime startDateTime, int durationMinutes) {
        LocalDateTime endDateTime = startDateTime.plusMinutes
                ((long) (durationMinutes * box.getWashingСoefficient().floatValue()));

        if (startDateTime.toLocalTime().isBefore(box.getOpeningTime()) ||
                endDateTime.toLocalTime().isAfter(box.getClosingTime())) {
            return false;
        }

        List<Reservation> reservations = reservationService.findRelevantReservationsByBox
                (box.getId(), startDateTime.minusHours(1), endDateTime.plusHours(1));

        for (Reservation reservation : reservations) {
            if (reservation.getEndDateTime().isAfter(startDateTime) &&
                    reservation.getStartDateTime().isBefore(endDateTime)) {
                return false;
            }
        }

        return true;
    }

    public Reservation createReservation(UUID washingId, LocalDateTime startDateTime) {
        Washing washing = washingService.getWashingById(washingId);
        if(washing == null) throw new IllegalArgumentException("Тип мойки не найден");

        Box suitableBox = findSuitableBox(washing, startDateTime);
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

        return reservationService.saveReservation(reservation);
    }

    public boolean updateReservation(UUID reservationId, UUID washingId, LocalDateTime startDateTime) {
        Washing washing = washingService.getWashingById(washingId);
        if(washing == null) throw new IllegalArgumentException("Тип мойки не найден");

        Reservation editReservation = reservationService.getReservationById(reservationId);
        if(editReservation.getWashing().getId() == washingId && editReservation.getStartDateTime().equals(startDateTime)){
            return false;
        }
        //Временно отменяем бронь чтобы при поиске учитывалось этот же время в боксе
        String editStatus = editReservation.getStatus();
        editReservation.setStatus(Status.CANCELLED.toString());
        reservationService.saveReservation(editReservation);

        Box suitableBox = findSuitableBox(washing, startDateTime);
        if (suitableBox == null) {
            editReservation.setStatus(editStatus);
            reservationService.saveReservation(editReservation);
            return false;
        }

        editReservation.setBox(suitableBox);
        editReservation.setWashing(washing);
        editReservation.setDiscount(0);
        editReservation.setResultPrice(washing.getPrice());
        editReservation.setStatus(Status.WAITING_RESERVATION.toString());
        editReservation.setStartDateTime(startDateTime);
        editReservation.setEndDateTime(startDateTime.plusMinutes((long)
                (washing.getDurationMinute() * suitableBox.getWashingСoefficient().floatValue())));

        reservationService.saveReservation(editReservation);

        return true;
    }
}
