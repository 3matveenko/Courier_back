package com.example.courier.service;

import com.example.courier.model.Setting;
import com.example.courier.model.data.AllSettings;
import com.example.courier.model.exception.TimeException;
import com.example.courier.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {

    @Autowired
    SettingRepository settingRepository;

    public void setTimerStartTime(String time){
        saveValueByKey("timer_start_time",time);
    }

    public void saveValueByKey(String _key, String _value){
        Setting setting = settingRepository.findByKey(_key).orElseThrow();
        setting.setValue(_value);
        settingRepository.save(setting);
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
                getValueByKey("crm_token"),
                getValueByKey("timer_sum"),
                getValueByKey("timer_start_time"),
                getValueByKey("angle"),
                getValueByKey("timer_sum_nodriver"),
                getValueByKey("fe_latitude"),
                getValueByKey("fe_longtitude"),
                getValueByKey("protocol"),
                getValueByKey("server_name"),
                getValueByKey("server_port"),
                getValueByKey("back_queue_name"),
                getValueByKey("rabbit_server_name"),
                getValueByKey("rabbit_server_port"),
                getValueByKey("rabbit_username"),
                getValueByKey("rabbit_password"),
                getValueByKey("order_distribution_principle"),
                getValueByKey("beginning_work")
        );
    }

    public void updatingSettings(AllSettings allSettings){
        saveValueByKey("timer_sum",allSettings.timerSum);
        saveValueByKey("timer_start_time",allSettings.timerStartTime);
        saveValueByKey("angle",allSettings.angle);
        saveValueByKey("timer_sum_nodriver",allSettings.timerSumNodriver);
        saveValueByKey("fe_latitude",allSettings.feLatitude);
        saveValueByKey("fe_longtitude",allSettings.feLongtitude);
        saveValueByKey("protocol",allSettings.protocol);
        saveValueByKey("server_name",allSettings.serverName);
        saveValueByKey("server_port",allSettings.serverPort);
        saveValueByKey("rabbit_server_name",allSettings.rabbitServerName);
        saveValueByKey("rabbit_server_port",allSettings.rabbitServerPort);
        saveValueByKey("rabbit_username",allSettings.rabbitUsername);
        saveValueByKey("rabbit_password",allSettings.rabbitPassword);
        saveValueByKey("order_distribution_principle",allSettings.orderDistributionPrinciple);
        saveValueByKey("beginning_work",allSettings.beginningWork);
    }
}
