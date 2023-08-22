package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="t_drivers")
public class Driver extends BaseEntity{

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    /**
     * Это статус занят/свободен от выполнения заказов.
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

}
