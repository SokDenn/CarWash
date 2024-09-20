package org.example.dto;

import lombok.Data;
import org.example.model.Box;
import org.example.model.Reservation;
import org.example.model.User;

import java.util.List;
import java.util.UUID;

/**
ДТО для отображения бронирований фронту
*/
@Data
public class ReservationListViewDTO {
    //Необходимые списки для отображения
    private List<Box> boxList;
    private List<Reservation> reservationList;

    // Фильтр
    private UUID boxIdFilter;
    private String startDateTimeFilter;
    private String endDateTimeFilter;
    private Boolean activeReservations;

    //Доход за период
    Long resultRevenue;

    // Авторизованный пользователь
    private User userAuthentication;

    //Комментарий по результату действия
    private String message;
}
