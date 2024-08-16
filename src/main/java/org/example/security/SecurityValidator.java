package org.example.security;

import org.example.Jwt.JwtRequest;
import org.example.model.Reservation;
import org.example.model.Role;
import org.example.model.User;
import org.example.service.ReservationService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SecurityValidator {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;

    public boolean canEditReservation(Reservation reservation, Authentication authentication) {

        User userAuthentication = userService.getAuthenticationUser();

        boolean isAdmin = userAuthentication.getRole().getName().equals("ADMIN");
        boolean isOperator = userAuthentication.getRole().getName().equals("OPERATOR");

        if (!isAdmin && !isOperator) {
            return reservation.getUser().getId().equals(userAuthentication.getId());

        } else if(isOperator){
            return reservation.getBox().getUserOperator().getId().equals(userAuthentication.getId())
                    || reservation.getUser().getId().equals(userAuthentication.getId());
        }

        return true;
    }

    public Authentication authenticate(JwtRequest authenticationRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );
    }
}
