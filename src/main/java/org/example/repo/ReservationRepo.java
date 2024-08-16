package org.example.repo;

import org.example.model.Box;
import org.example.model.Reservation;
import org.example.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

public interface ReservationRepo extends CrudRepository<Reservation, UUID>, ReservationRepoCastom {

    @Query("SELECT r FROM Reservation r WHERE r.isDeleted = false AND r.box.id = :boxId " +
            "AND r.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND r.startDateTime BETWEEN :startDateTime AND :endDateTime")
    List<Reservation> findRelevantReservationsByBox(
            @Param("boxId") UUID boxId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    List<Reservation> findByUserId(UUID userId);

    List<Reservation> findByStatus(String status);

}

