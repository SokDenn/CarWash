package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class Reservation {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "box_id")
    private Box box;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "creation_date_time")
    private LocalDateTime creationDateTime;

    private String status;

    @ManyToOne
    @JoinColumn(name = "washing_id")
    private Washing washing;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "result_price")
    private Integer resultPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Reservation() {
    }

    public Reservation(Box box, LocalDateTime startDateTime, LocalDateTime endDateTime,
                       Washing washing, User user) {
        this.box = box;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.creationDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        this.status = Status.WAITING_RESERVATION.toString();
        this.washing = washing;
        this.resultPrice = washing.getPrice();
        this.discount = 0;
        this.user = user;
        this.isDeleted = false;
    }
}
