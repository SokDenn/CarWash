package org.example.repo;

import org.example.model.Reservation;

import java.util.List;
import java.util.UUID;

public interface ReservationRepoCustom {
    List<Reservation> findReservations(UUID boxId, String startDataTime,
                                       String endDateTime, List<String> statusList);
}
