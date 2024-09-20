package org.example.service;

import org.example.dto.ReservationDTO;
import org.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления бронированием боксов для мойки.
 */
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

    /**
     * Найти подходящий бокс для мойки с учетом времени начала и текущей брони.
     *
     * @param washing тип мойки
     * @param startDateTime время начала
     * @param currentReservation текущая бронь для учета
     * @return подходящий бокс или null, если таковой не найден
     */
    public Box findSuitableBox(Washing washing, LocalDateTime startDateTime, Reservation currentReservation) {
        List<Box> availableBoxes = boxService.findAllActive();

        // Фильтруем боксы по времени открытия, закрытия и наличию пересекающихся бронирований
        List<Box> suitableBoxes = availableBoxes.stream()
                .filter(box -> isBoxAvailable(box, startDateTime, washing.getDurationMinute(), currentReservation))
                .sorted(Comparator.comparing(Box::getWashingСoefficient))
                .toList();

        return suitableBoxes.isEmpty() ? null : suitableBoxes.get(0);
    }

    /**
     * Проверить, доступен ли указанный бокс для бронирования в заданное время.
     *
     * @param box бокс для проверки
     * @param startDateTime время начала
     * @param durationMinutes длительность мойки в минутах
     * @param currentReservation текущая бронь (может быть null)
     * @return true, если бокс доступен, иначе false
     */
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

    /**
     * Создать новую бронь.
     *
     * @param washingId ID типа мойки
     * @param startDateTime время начала бронирования
     * @return объект ReservationDTO с информацией о созданной броне
     */
    public ReservationDTO createReservation(UUID washingId, LocalDateTime startDateTime) {
        Washing washing = washingService.getWashingById(washingId);
        if (washing == null) {
            throw new IllegalArgumentException("Тип мойки не найден");
        }

        Box suitableBox = findSuitableBox(washing, startDateTime, null);
        ReservationDTO reservationDTO = new ReservationDTO();

        if (suitableBox == null) {
            reservationDTO.setSelectedStartDateTime(startDateTime);
            reservationDTO.setSelectedWashingId(washingId);
            reservationDTO.setMessage("На это время нет свободных боксов! Выберите другое");
            return reservationDTO;
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

        reservationDTO.setReservation(reservation);
        reservationDTO.setMessage("На почту отправлена ссылка для подтверждения");
        return reservationDTO;
    }

    /**
     * Обновить существующую бронь.
     *
     * @param reservationId ID брони для обновления
     * @param washingId новый ID типа мойки
     * @param startDateTime новое время начала бронирования
     * @return объект ReservationDTO с информацией об обновленной броне
     */
    public ReservationDTO updateReservation(UUID reservationId, UUID washingId, LocalDateTime startDateTime) {
        Washing washing = washingService.getWashingById(washingId);
        if (washing == null) {
            throw new IllegalArgumentException("Тип мойки не найден");
        }

        Reservation editReservation = reservationService.getReservationById(reservationId);
        Box suitableBox = findSuitableBox(washing, startDateTime, editReservation);
        ReservationDTO reservationDTO = new ReservationDTO();

        if (isUnchangedOrCompleted(editReservation, washingId, startDateTime) || suitableBox == null) {
            reservationDTO.setMessage("Нет свободных боксов / изменения не были внесены / бронь в конечном статусе");
            return reservationDTO;
        }

        updateReservationDetails(editReservation, washing, suitableBox, startDateTime);
        reservationService.saveReservation(editReservation);

        String confirmationLink = "/api/reservations/editReservation/confirm/" + reservationId;
        System.out.println("Для подтверждения брони перейдите по ссылке: " + confirmationLink);
        reservationDTO.setMessage("Забронировано новое время. На почту отправлена ссылка для подтверждения");
        return reservationDTO;
    }

    /**
     * Проверить, изменилась ли бронь и в конечном ли она статусе.
     *
     * @param reservation бронь для проверки
     * @param washingId новый ID типа мойки
     * @param startDateTime новое время начала бронирования
     * @return true, если бронь не изменялась или в конечном статусе, иначе false
     */
    private boolean isUnchangedOrCompleted(Reservation reservation, UUID washingId, LocalDateTime startDateTime) {
        return reservation.getWashing().getId().equals(washingId)
                && reservation.getStartDateTime().equals(startDateTime)
                || reservation.getStatus().equals(Status.COMPLETED.toString())
                || reservation.getStatus().equals(Status.CANCELLED.toString());
    }

    /**
     * Обновить детали бронирования.
     *
     * @param reservation бронь для обновления
     * @param washing новый тип мойки
     * @param box новый бокс
     * @param startDateTime новое время начала
     */
    private void updateReservationDetails(Reservation reservation, Washing washing, Box box, LocalDateTime startDateTime) {
        reservation.setBox(box);
        reservation.setWashing(washing);
        reservation.setDiscount(0);
        reservation.setResultPrice(washing.getPrice());
        reservation.setStatus(Status.WAITING_RESERVATION.toString());
        reservation.setStartDateTime(startDateTime);
        reservation.setEndDateTime(calculateEndDateTime(startDateTime, washing, box));
    }

    /**
     * Рассчитать время окончания бронирования на основе времени начала, типа мойки и бокса.
     *
     * @param startDateTime время начала бронирования
     * @param washing тип мойки
     * @param box выбранный бокс
     * @return время окончания бронирования
     */
    private LocalDateTime calculateEndDateTime(LocalDateTime startDateTime, Washing washing, Box box) {
        return startDateTime.plusMinutes((long)
                (washing.getDurationMinute() * box.getWashingСoefficient().floatValue()));
    }
}
