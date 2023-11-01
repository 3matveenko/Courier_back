package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="t_rejects")
public class RejectOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "comment")
    private String comment;

    @OneToOne
    private Driver driver;

    @Column(name = "date")
    private Date date;

    @Column(name = "latitude")
    private double latitude;

    @Column(name="longitude")
    private double longitude;


    public RejectOrder(String comment, Driver driver, Date date, double latitude, double longitude) {
        this.comment = comment;
        this.driver = driver;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public RejectOrder() {
    }
}
