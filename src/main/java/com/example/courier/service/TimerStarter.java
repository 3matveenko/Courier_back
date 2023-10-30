package com.example.courier.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimerStarter {

    @Autowired
    TimerService timerService;

    public void startTimerSum(){
        timerService.startTimerSum();
    }

    public void stopTimerSum(){
        timerService.stopTimerSum();
    }

    public void startNoDriverTimer(){
        timerService.stopNoDriverTimer();
    }

    public void stopNoDriverTimer(){
        timerService.stopNoDriverTimer();
    }
}
