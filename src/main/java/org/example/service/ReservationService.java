package org.example.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.example.converter.StringConverter;
import org.example.model.*;
import org.example.repo.ReservationRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<Reservation> findRelevantReservationsByBox(UUID boxId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return reservationRepo.findRelevantReservationsByBox(boxId, startDateTime, endDateTime);
    }

    public List<Reservation> returnFilteredReservationList(UUID boxId, String startDataTimeStr,
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

        return reservationRepo.findReservations(boxId, startDataTimeStr, endDateTimeStr, statusList);
    }

    public void deleteReservation(UUID id) {
        Reservation reservation = reservationRepo.findById(id).orElse(null);

        if (reservation != null) {
            reservation.setIsDeleted(true);
            reservationRepo.save(reservation);
        }
    }

    public Reservation getReservationById(UUID id) {
        return reservationRepo.findById(id).orElse(null);
    }

    public List<Reservation> findByUserId(UUID userId) {
        return reservationRepo.findByUserId(userId);
    }

    public List<Reservation> findByStatus(String status) {
        return reservationRepo.findByStatus(status);
    }

    public Reservation saveReservation(Reservation reservation) {
        return reservationRepo.save(reservation);
    }

    public List<Reservation> returnCompletedReservationList(String startDateTimeStr, String endDateTimeStr) {

        List<String> statusList = new ArrayList<>();
        statusList.add(Status.COMPLETED.toString());

        return reservationRepo.findReservations(null, startDateTimeStr, endDateTimeStr, statusList);
    }

    public Long calculateRevenue(List<Reservation> reservationList) {

        Long resultRevenue = 0L;

        for (Reservation reservation : reservationList) {
            resultRevenue += reservation.getResultPrice();
        }
        return resultRevenue;
    }

    public void updateReservationDiscount(UUID reservationId, Integer discount){
        Reservation reservation = getReservationById(reservationId);
        Integer washingPrice = reservation.getWashing().getPrice();

        reservation.setResultPrice(washingPrice - (washingPrice/100*discount));
        reservation.setDiscount(discount);
        saveReservation(reservation);
    }
}
