package com.example.courier.service;

import com.example.courier.model.data.AllSettings;
import com.example.courier.model.exception.TimeException;
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

    public String getTimeOrderChange() throws TimeException {
        String time = settingRepository.findByKey("timer_start_time").orElseThrow().getValue();
        if("no_time".equals(time)){
            throw new TimeException("no_time");
        } else {
            return time;
        }
    }
    public AllSettings collectingSettings(){
        return new AllSettings(
                settingRepository.findByKey("crm_token").orElseThrow().getValue(),
                settingRepository.findByKey("timer_sum").orElseThrow().getValue(),
                settingRepository.findByKey("timer_start_time").orElseThrow().getValue(),
                settingRepository.findByKey("angle").orElseThrow().getValue(),
                settingRepository.findByKey("timer_sum_nodriver").orElseThrow().getValue(),
                settingRepository.findByKey("fe_latitude").orElseThrow().getValue(),
                settingRepository.findByKey("fe_longtitude").orElseThrow().getValue(),
                settingRepository.findByKey("protocol").orElseThrow().getValue(),
                settingRepository.findByKey("server_name").orElseThrow().getValue(),
                settingRepository.findByKey("server_port").orElseThrow().getValue(),
                settingRepository.findByKey("back_queue_name").orElseThrow().getValue(),
                settingRepository.findByKey("rabbit_server_name").orElseThrow().getValue(),
                settingRepository.findByKey("rabbit_server_port").orElseThrow().getValue(),
                settingRepository.findByKey("rabbit_username").orElseThrow().getValue(),
                settingRepository.findByKey("rabbit_password").orElseThrow().getValue(),
                settingRepository.findByKey("order_distribution_principle").orElseThrow().getValue(),
                settingRepository.findByKey("beginning_work").orElseThrow().getValue()
        );
    }
}
