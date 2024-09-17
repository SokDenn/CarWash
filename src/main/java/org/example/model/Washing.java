package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name = "washings")
public class Washing {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String name;

    private int price;

    @Column(name = "duration_minute")
    private int durationMinute;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public Washing() {
    }

    public Washing(String name, int price, int durationMinute) {
        this.name = name;
        this.price = price;
        this.durationMinute = durationMinute;
        this.isDeleted = false;
    }
}
