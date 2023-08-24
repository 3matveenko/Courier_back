package com.example.courier.service;

import com.example.courier.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {

    @Autowired
    SettingRepository settingRepository;

    public void setTimeInDb(String time){
        settingRepository.findByKey("timer_start_time").orElseThrow().setValue(time);
    }

    public String getValueByKey(String key){
       return settingRepository.findByKey(key).orElseThrow().getValue();
    }
}
