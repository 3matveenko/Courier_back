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

import java.util.Date;
import java.util.List;
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

    public List<Driver> getAll(){
        return driverRepository.findAll();
    }

    public void changeDriverOrderStatus(Long driverId){
        Driver driver = driverRepository.findById(driverId).orElseThrow();
      if(driver.isStatusOrder()){
          driver.setTimeFree(null);
          driver.setStatusOrder(false);
      } else {
          driver.setTimeFree(new Date());
          driver.setStatusOrder(true);
      }
      driverRepository.save(driver);
    }

    public void deleteById(Long driverId){
        driverRepository.delete(driverRepository.findById(driverId).orElseThrow());
    }

    public List<Driver> findAllByStatusOrder(Boolean status){
        return driverRepository.findAllByStatusOrder(status);
    }

}
