package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Entity
public class Box {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "box_number")
    private int boxNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_operator_id")
    private User userOperator;

    @Column(name = "washing_coefficient", precision = 10, scale = 4)
    private BigDecimal washingСoefficient;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Box() {
    }

    public Box(int boxNumber, BigDecimal washingСoefficient, LocalTime openingTime, LocalTime closingTime) {
        this.boxNumber = boxNumber;
        this.washingСoefficient = washingСoefficient;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.isDeleted = false;
    }
}
