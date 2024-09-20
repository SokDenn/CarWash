package org.example.repo;

import org.example.model.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Reservation.
 */
public interface ReservationRepo extends CrudRepository<Reservation, UUID>, ReservationRepoCustom {

    /**
     * Найти активные бронирования для определенного бокса в заданном временном диапазоне.
     * Учитываются только бронирования, которые не отменены и не завершены.
     *
     * @param boxId идентификатор бокса
     * @param startDateTime начало временного диапазона
     * @param endDateTime конец временного диапазона
     * @return список активных бронирований для данного бокса в указанное время
     */
    @Query("SELECT r FROM Reservation r WHERE r.isDeleted = false AND r.box.id = :boxId " +
            "AND r.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND r.startDateTime BETWEEN :startDateTime AND :endDateTime")
    List<Reservation> findRelevantReservationsByBox(
            @Param("boxId") UUID boxId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Найти все бронирования, связанные с определенным пользователем по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return список бронирований пользователя
     */
    List<Reservation> findByUserId(UUID userId);

    /**
     * Найти все бронирования по их статусу.
     *
     * @param status статус бронирования (например, 'CONFIRMED', 'CANCELLED')
     * @return список бронирований с указанным статусом
     */
    List<Reservation> findByStatus(String status);

}

