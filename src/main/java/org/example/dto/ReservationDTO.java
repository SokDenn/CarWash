package org.example.dto;

import lombok.Data;
import org.example.model.Box;
import org.example.model.Reservation;
import org.example.model.User;
import org.example.model.Washing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
ДТО для передачи информации о брони для фронта
*/
@Data
public class ReservationDTO {
    //Бронь
    private UUID id;
    private Box box;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime creationDateTime;
    private String status;
    private Washing washing;
    private Integer discount;
    private Integer resultPrice;
    private User user;
    private Boolean isDeleted;

    //Доступные кнопки работы с бронью
    private Map<String, String> statusChangeButtons;

    //Список доступных скидок
    private List<Integer> discountList;

    //Список доступных услуг
    private List<Washing> washingList;

    // Выбранные поля услуги у брони
    private LocalDateTime selectedStartDateTime;
    private UUID selectedWashingId;

    //Комментарий по результату действия
    private String message;

    public ReservationDTO() {
    }

    public void setReservation(Reservation reservation) {
        this.id = reservation.getId();
        this.box = reservation.getBox();
        this.startDateTime = reservation.getStartDateTime();
        this.endDateTime = reservation.getEndDateTime();
        this.creationDateTime = reservation.getCreationDateTime();
        this.status = reservation.getStatus();
        this.washing = reservation.getWashing();
        this.resultPrice = reservation.getResultPrice();
        this.discount = reservation.getDiscount();
        this.user = reservation.getUser();
        this.isDeleted = reservation.getIsDeleted();
    }
}
