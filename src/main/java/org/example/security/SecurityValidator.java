package org.example.security;

import org.example.Jwt.JwtRequest;
import org.example.dto.ReservationDTO;
import org.example.model.Reservation;
import org.example.model.User;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityValidator {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private WashingService washingService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private ReservationStatusService reservationStatusService;

    public ReservationDTO canEditReservation(Reservation reservation, UUID reservationId) {

        User userAuthentication = userService.getAuthenticationUser();
        ReservationDTO reservationDTO = new ReservationDTO();

        boolean isAdmin = userAuthentication.getRole().getName().equals("ADMIN");
        boolean isOperator = userAuthentication.getRole().getName().equals("OPERATOR");

        if (!isAdmin) {
            // Проверка прав для обычного пользователя и оператора
            boolean canEdit = reservation.getUser().getId().equals(userAuthentication.getId())
                    || (isOperator && reservation.getBox().getUserOperator().getId().equals(userAuthentication.getId()));

            if (!canEdit) {
                reservationDTO.setMessage("Вы не можете редактировать не свою запись!");
                return reservationDTO;
            }
        }

        reservationDTO.setReservation(reservationService.getReservationById(reservationId));
        reservationDTO.setWashingList(washingService.getAllWashing());
        reservationDTO.setDiscountList(discountService.getPermittedDiscountList());
        reservationDTO.setStatusChangeButtons(reservationStatusService.getStatusChangeButtons(reservationId));
        return reservationDTO;
    }

    public Authentication authenticate(JwtRequest authenticationRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );
    }

    public boolean isAuthenticated() {
        try {
            userService.getAuthenticationUser();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
