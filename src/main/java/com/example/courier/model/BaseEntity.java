package com.example.courier.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

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
}
