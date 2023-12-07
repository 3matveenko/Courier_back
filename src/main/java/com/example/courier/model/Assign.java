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

    @OneToMany(fetch = FetchType.EAGER)
    private List<Order> orders;

    @Column(name = "time_start")
    private Date timeStart;

    @Column(name = "time_run")
    private Date timeRun;

    @Column(name = "time_end")
    private Date timeEnd  = new Date(0L);

    public Date getTimeStartAlmaty(){
        return  new Date(timeStart.getTime()+(6*60*60*1000));
    }

    public Date getTimeRunAlmaty(){
        if(timeRun!=null){
            return  new Date(timeRun.getTime()+(6*60*60*1000));
        } else {
            return new Date(0L);
        }
    }

    public Date getTimeEndAlmaty(){
        return new Date(timeEnd.getTime()+(6*60*60*1000));
    }

}
