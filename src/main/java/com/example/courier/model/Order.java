package com.example.courier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="t_orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    /**
     * -1 - отложенный заказ(еще не настало время распределения)
     * 0  - принят
     * 1  - в пути
     * 2  - доставлен
     */
    @Column(name = "status_delivery")
    private int statusDelivery;

    @Column(name = "guid")
    private String guid;

    @Column(name = "date_start")
    private Date dateStart;

    @Column(name = "date_end")
    private Date dateEnd = new Date(0L);

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
    private Double angle;

    @OneToOne
    private RejectOrder rejectOrder;

    @Column
    private String comment;

    /**
     * Заказ был отправлен с подтверждением по смс да-true/нет-false
     */
    @Column(name = "send_sms")
    private Boolean sendSmS;

    @Column
    @Transient
    private Date delivery;

    @Column
    @Transient
    private String driver_token;

    public Date getTimeStartAlmaty(){
        return  new Date(dateStart.getTime()+(6*60*60*1000));
    }

    public Date getTimeEndAlmaty(){
        return   new Date(dateEnd.getTime()+(6*60*60*1000));
    }
}
