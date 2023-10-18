package com.example.courier.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;


@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="t_assigns")
public class Assign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @OneToOne
    private Driver driver;

    @OneToMany
    private List<Order> orders;

    @Column(name = "time_open")
    private Date timeStart;

    @Column(name = "time_open")
    private Date timeEnd;

    @Column(name = "comment")
    private String comment;


}
