package com.example.courier.service;

import com.example.courier.model.Driver;
import com.example.courier.model.exception.AuthoryException;
import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.repository.DriverRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DriverService {

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<String> create(String json) throws AuthoryException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Driver driver = objectMapper.readValue(json, Driver.class);
        if(driverRepository.findByLogin(driver.getLogin()).isEmpty()){
            String token = UUID.randomUUID().toString();
            driver.setPassword(passwordEncoder.encode(driver.getPassword()));
            driver.setToken(token);
            driver.setStatusDay(false);
            driver.setStatusOrder(false);
            driverRepository.save(driver);
            return ResponseEntity.ok(token);
        } else {
            throw new AuthoryException("505");
        }
    }

    public ResponseEntity<String> authorization(String json) throws JsonProcessingException, ForbiddenException {
        ObjectMapper objectMapper = new ObjectMapper();
        Driver driver = objectMapper.readValue(json, Driver.class);
        Optional<Driver> driverOld = driverRepository.findByLogin(driver.getLogin());
        if(driverOld.isPresent()&&passwordEncoder.matches(driver.getPassword(), driverOld.get().getPassword())){
            return ResponseEntity.ok(driverOld.get().getToken());
        } else {
            throw new ForbiddenException("Forbidden");
        }
    }

}
