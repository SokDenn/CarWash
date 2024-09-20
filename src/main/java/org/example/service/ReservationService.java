package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.converter.StringConverter;
import org.example.dto.ReservationListViewDTO;
import org.example.model.Reservation;
import org.example.model.Status;
import org.example.repo.ReservationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления бронированиями.
 */
@Service
public class ReservationService {
    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private BoxService boxService;
    @Autowired
    private WashingService washingService;
    @Autowired
    private StringConverter stringConverter;

    /**
     * Найти все брони, соответствующие указанному боксу и диапазону времени.
     *
     * @param boxId ID бокса
     * @param startDateTime время начала
     * @param endDateTime время окончания
     * @return список соответствующих бронирований
     */
    public List<Reservation> findRelevantReservationsByBox(UUID boxId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return reservationRepo.findRelevantReservationsByBox(boxId, startDateTime, endDateTime);
    }


    /**
     * Вернуть отфильтрованный список броней на основе указанных параметров.
     *
     * @param boxId ID бокса
     * @param startDateTimeStr строка с временем начала
     * @param endDateTimeStr строка с временем окончания
     * @param activeReservations флаг для получения активных или завершенных броней
     * @return отфильтрованный список броней
     */
    public List<Reservation> returnFilteredReservationList(UUID boxId, String startDateTimeStr,
                                                           String endDateTimeStr, Boolean activeReservations) {
        List<String> statusList = new ArrayList<>();

        if (activeReservations != null && activeReservations) {
            statusList.add(Status.WAITING_RESERVATION.toString());
            statusList.add(Status.BOOKED.toString());
            statusList.add(Status.AT_CAR_WASH.toString());
        }

        if (activeReservations != null && !activeReservations) {
            statusList.add(Status.CANCELLED.toString());
            statusList.add(Status.COMPLETED.toString());
        }

        LocalDateTime[] dateTimeRange = stringConverter.parseDateTimeRange(startDateTimeStr, endDateTimeStr);
        return reservationRepo.findReservations(boxId, dateTimeRange[0], dateTimeRange[1], statusList);
    }

    /**
     * Удалить бронирование по указанному ID.
     *
     * @param id ID бронирования
     */
    public void deleteReservation(UUID id) {
        Reservation reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation c ID: " + id + " не найден"));

        if (reservation != null) {
            reservation.setIsDeleted(true);
            reservationRepo.save(reservation);
        }
    }

    /**
     * Получить бронь по указанному ID.
     *
     * @param id ID бронирования
     * @return объект Reservation или null, если не найдено
     */
    public Reservation getReservationById(UUID id) {
        return reservationRepo.findById(id).orElse(null);
    }

    /**
     * Найти все бронирования для указанного пользователя.
     *
     * @param userId ID пользователя
     * @return список броней пользователя
     */
    public List<Reservation> findByUserId(UUID userId) {
        return reservationRepo.findByUserId(userId);
    }

    /**
     * Найти все бронирования по статусу.
     *
     * @param status статус для фильтрации
     * @return список броней с указанным статусом
     */
    public List<Reservation> findByStatus(String status) {
        return reservationRepo.findByStatus(status);
    }

    /**
     * Сохранить или обновить бронь.
     *
     * @param reservation объект Reservation для сохранения
     * @return сохраненный объект Reservation
     */
    public Reservation saveReservation(Reservation reservation) {
        return reservationRepo.save(reservation);
    }

    /**
     * Получить список завершенных броней в указанном диапазоне времени.
     *
     * @param startDateTimeStr строка с временем начала
     * @param endDateTimeStr строка с временем окончания
     * @return список завершенных резервирований
     */
    public List<Reservation> getCompletedReservationList(String startDateTimeStr, String endDateTimeStr) {

        List<String> statusList = new ArrayList<>();
        statusList.add(Status.COMPLETED.toString());

        LocalDateTime[] dateTimeRange = stringConverter.parseDateTimeRange(startDateTimeStr, endDateTimeStr);
        return reservationRepo.findReservations(null, dateTimeRange[0], dateTimeRange[1], statusList);
    }

    /**
     * Рассчитать общую выручку из списка броней.
     *
     * @param reservationList список бронирований
     * @return сумма выручки
     */
    public Long calculateRevenue(List<Reservation> reservationList) {

        return reservationList.stream()
                .mapToLong(Reservation::getResultPrice)
                .sum();
    }

    /**
     * Обновить скидку для указанной брони.
     *
     * @param reservationId ID бронирования
     * @param discount размер скидки
     */
    public void updateReservationDiscount(UUID reservationId, Integer discount) {
        Reservation reservation = getReservationById(reservationId);
        Integer washingPrice = reservation.getWashing().getPrice();

        reservation.setResultPrice(washingPrice - (washingPrice / 100 * discount));
        reservation.setDiscount(discount);
        saveReservation(reservation);
    }

    /**
     * Получить отфильтрованный список резервирований и, при необходимости, общую выручку.
     *
     * @param boxIdFilter фильтр по ID бокса
     * @param startDateTimeStr строка с временем начала
     * @param endDateTimeStr строка с временем окончания
     * @param activeReservations флаг для получения активных броней
     * @param displayRevenue флаг для отображения выручки
     * @return объект ReservationListViewDTO с результатами
     */
    public ReservationListViewDTO getFilteredReservations(UUID boxIdFilter, String startDateTimeStr, String endDateTimeStr,
                                                          Boolean activeReservations, Boolean displayRevenue) {

        ReservationListViewDTO reservationListViewDTO = new ReservationListViewDTO();

        if (userService.isAdmin() && displayRevenue != null) {
            reservationListViewDTO.setReservationList(
                    getCompletedReservationList(startDateTimeStr, endDateTimeStr));

            reservationListViewDTO.setResultRevenue(
                    calculateRevenue(reservationListViewDTO.getReservationList()));

        } else {
            reservationListViewDTO.setReservationList(
                    returnFilteredReservationList(boxIdFilter, startDateTimeStr, endDateTimeStr, activeReservations));

            if (displayRevenue != null && !userService.isAdmin()) {
                reservationListViewDTO.setMessage("Функция доступна только админу!");

            }
        }

        reservationListViewDTO.setActiveReservations(activeReservations);
        reservationListViewDTO.setBoxIdFilter(boxIdFilter);
        reservationListViewDTO.setStartDateTimeFilter(startDateTimeStr);
        reservationListViewDTO.setEndDateTimeFilter(endDateTimeStr);
        reservationListViewDTO.setUserAuthentication(userService.getAuthenticationUser());
        reservationListViewDTO.setBoxList(boxService.findAllActive());

        return reservationListViewDTO;
    }
}
