package com.example.courier.service;

import com.example.courier.model.exception.DriversIsEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class TimerService {

    @Autowired
    SettingService settingService;

    @Autowired
    OrderService orderService;

    static Timer timerSum;

    static Timer timerNoDriver;


    /**
     0- остановить таймер
     1- новый таймер
     2- таймер если водителей не найдено
     */

    public void startTimerSum(){
        Timer timerSum = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    orderService.changeOrders(orderService.appointmentDriverAuto());
                    settingService.setTimerStartTime("none");
                    timerSum.cancel();
                } catch (DriversIsEmptyException e){
                    timerSum.cancel();
                    startNoDriverTimer();
                }

            }
        };
        settingService.setTimerStartTime(new SimpleDateFormat("HH:mm").format(new Date()));
        timerSum.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum")) * 60 * 1000);
    }

    public void stopTimerSum(){
        settingService.setTimerStartTime("none");
        timerSum.cancel();
    }

    public void startNoDriverTimer(){
        timerNoDriver = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    settingService.setTimerStartTime("none");
                    timerNoDriver.cancel();
                    orderService.changeOrders(orderService.appointmentDriverAuto());
                } catch (DriversIsEmptyException e){
                    timerNoDriver.cancel();
                    startNoDriverTimer();
                }
            }
        };
        settingService.setTimerStartTime(new SimpleDateFormat("HH:mm").format(new Date()));
        timerNoDriver.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum_nodriver")) * 60 * 1000);
    }

    public void stopNoDriverTimer(){
        timerNoDriver.cancel();
    }
}
