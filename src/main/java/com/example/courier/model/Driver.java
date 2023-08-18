package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="t_drivers")
public class Driver extends BaseEntity{

    @Column(name = "name")
    private String name;

    @Column(name = "imei")
    private int imei;

    @Column(name = "status")
    private boolean status;

    @OneToOne(fetch = FetchType.EAGER)
    private Gps gps;

}
