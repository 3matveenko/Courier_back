package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="t_orders")
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    /**
     * 0 - принят
     * 1 - в пути
     * 2 - доставлен
     */
    @Column(name = "status_delivery")
    private int statusDelivery;

    @Column(name = "guid")
    private String guid;

    @Column(name = "date_start")
    private Date dateStart;

    @Column(name = "date_end")
    private Date dateEnd;

    @Column(name = "addres")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "current")
    private String current;

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

    @OneToOne
    private Driver driver;

    /**
     * Угол между вектрами:
     * - fe/северный полюс
     * - fe/наш заказ
     */
    @Column(name = "angle")
    private double angle;

}
