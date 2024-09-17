package org.example.repo;

import org.example.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepoCustom {
    List<Reservation> findReservations(UUID boxId, LocalDateTime startDataTime,
                                       LocalDateTime endDateTime, List<String> statusList);
}
