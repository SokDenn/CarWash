package org.example.repo;

import org.example.model.Reservation;
import org.example.model.Status;

import java.util.List;
import java.util.Stack;
import java.util.UUID;

public interface ReservationRepoCastom {
    List<Reservation> findReservations(UUID boxId, String startDataTime,
                                       String endDateTime, List<String> statusList);
}
