package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.converter.StringConverter;
import org.example.dto.ReservationDTO;
import org.example.model.Reservation;
import org.example.model.Status;
import org.example.repo.ReservationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void deleteReservation(UUID id) {
        Reservation reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation c ID: " + id + " не найден"));

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

        LocalDateTime[] dateTimeRange = stringConverter.parseDateTimeRange(startDateTimeStr, endDateTimeStr);
        return reservationRepo.findReservations(null, dateTimeRange[0], dateTimeRange[1], statusList);
    }

    public Long calculateRevenue(List<Reservation> reservationList) {

        return reservationList.stream()
                .mapToLong(Reservation::getResultPrice)
                .sum();
    }

    public void updateReservationDiscount(UUID reservationId, Integer discount) {
        Reservation reservation = getReservationById(reservationId);
        Integer washingPrice = reservation.getWashing().getPrice();

        reservation.setResultPrice(washingPrice - (washingPrice / 100 * discount));
        reservation.setDiscount(discount);
        saveReservation(reservation);
    }

    public ReservationDTO getFilteredReservations(UUID boxIdFilter, String startDateTimeStr, String endDateTimeStr,
                                                  Boolean activeReservations, Boolean displayRevenue) {

        ReservationDTO reservationDTO = new ReservationDTO();

        if (userService.isAdmin() && displayRevenue != null) {
            reservationDTO.setReservationList(
                    returnCompletedReservationList(startDateTimeStr, endDateTimeStr));

            reservationDTO.setResultRevenue(
                    calculateRevenue(reservationDTO.getReservationList()));

        } else if (displayRevenue != null && !userService.isAdmin()) {
            reservationDTO.setMessage("Функция доступна только админу!");

        } else {
            reservationDTO.setReservationList(
                    returnFilteredReservationList(boxIdFilter, startDateTimeStr, endDateTimeStr, activeReservations));
        }

        reservationDTO.setActiveReservations(activeReservations);
        reservationDTO.setBoxIdFilter(boxIdFilter);
        reservationDTO.setStartDateTimeFilter(startDateTimeStr);
        reservationDTO.setEndDateTimeFilter(endDateTimeStr);
        reservationDTO.setUserAuthentication(userService.getAuthenticationUser());
        reservationDTO.setBoxList(boxService.findAllActive());

        return reservationDTO;
    }
}
