package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="t_drivers")
public class Driver{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    /**
     * Это статус занят/свободен от выполнения заказов.
     * true - свободен
     * false - занят
     */
    @Column(name = "status_order")
    private boolean statusOrder;

    /**
     * Это статус на смене/выходной.
     * (Если хотябы раз водитель поставил статус
     * свободен то он оказывается на смене.
     * В полночь все водители снимаются со смены)
     */
    @Column(name = "status_day")
    private boolean statusDay;

    @Column(name = "token")
    private String token;

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

    /**
     * Время установки статуса "cвободен"
     */
    @Column(name = "time_free")
    private Date timeFree;

    /**
     * Время отправки координат
     */
    @Column(name = "last_update_location")
    private Date lastUpdateLocation;

    /**
     * Время последней установки статуса "cвободен". Нужно для удаления не активных водителей
     */
    @Column(name = "time_free_today")
    private Date timeFreeToday;
}
