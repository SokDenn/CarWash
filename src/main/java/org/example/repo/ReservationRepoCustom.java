package org.example.repo;

import org.example.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Кастомный интерфейс для расширения функциональности работы с сущностью Reservation.
 */
public interface ReservationRepoCustom {

    /**
     * Найти бронирования для конкретного бокса в заданном временном диапазоне и с указанными статусами.
     *
     * @param boxId идентификатор бокса
     * @param startDataTime начало временного диапазона
     * @param endDateTime конец временного диапазона
     * @param statusList список статусов для фильтрации бронирований (например, 'CONFIRMED', 'CANCELLED')
     * @return список бронирований, соответствующих указанным критериям
     */
    List<Reservation> findReservations(UUID boxId, LocalDateTime startDataTime,
                                       LocalDateTime endDateTime, List<String> statusList);
}
