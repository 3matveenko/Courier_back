package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="t_gps")
public class Gps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "type")
    private String type;

    /**
     * Широта
     */
    @Column(name = "latitude")
    private double latitude;

    /**
     * Долгота
     */
    @Column(name="longitude")
    private double longitude;

    @Column(name = "date")
    private Date date;
}
