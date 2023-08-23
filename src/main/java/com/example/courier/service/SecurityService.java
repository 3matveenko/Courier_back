package com.example.courier.service;

import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    @Autowired
    SettingRepository settingRepository;
    public Boolean crmSecurity(String token)  throws ForbiddenException {
    if(settingRepository.findByKey("crm_token").get().equals(token)){
        return true;
    } else {
        throw new ForbiddenException("Invalid token");
    }
    }
}
