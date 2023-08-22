package com.example.courier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="t_orders")
public class Order extends BaseEntity{

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


}
