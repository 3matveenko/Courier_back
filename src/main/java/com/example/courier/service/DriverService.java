package com.example.courier.service;

import com.example.courier.model.Driver;
import com.example.courier.model.data.Location;
import com.example.courier.model.data.Message;
import com.example.courier.model.exception.AuthoryException;
import com.example.courier.model.exception.ForbiddenException;
import com.example.courier.repository.DriverRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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

    @Autowired
    RabbitService rabbitService;

    @Autowired
    AssignService assignService;

    public ResponseEntity<String> create(String json) throws AuthoryException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Driver driver = objectMapper.readValue(json, Driver.class);
        if(driverRepository.findByLogin(driver.getLogin()).isEmpty()){
            String token = UUID.randomUUID().toString();
            driver.setPassword(passwordEncoder.encode(driver.getPassword()));
            driver.setToken(token);
            rabbitService.createDriverQueue(token);
            rabbitService.createExchange(token);
            driver.setStatusDay(false);
            driver.setStatusOrder(false);
            driverRepository.save(driver);
            System.out.println("ответ = "+ResponseEntity.ok(token));
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
            return ResponseEntity.ok("ok");
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

    public void save(Driver driver){
        driverRepository.save(driver);
    }

    public Driver findDriverByToken(String token){
        return driverRepository.findByToken(token).orElseThrow();
    }
    public void getLocation(Message message){
        Gson gson = new Gson();
        Location location = gson.fromJson(message.getBody(), Location.class);
        Driver driver = findDriverByToken(message.getToken());
        driver.setLastUpdateLocation(new Date(message.getMillisecondsSinceEpoch()));
        driver.setLatitude(location.getLatitude());
        driver.setLongitude(location.getLongitude());
        assignService.checkAssignStatus(driver);
        save(driver);

    }


    public Boolean getStatusDayByToken(String token, Boolean flag) throws AuthoryException{
        Optional<Driver> optional = driverRepository.findByToken(token);
        if(optional.isPresent()){
            Driver driver = optional.get();
            if(flag){
                driver.setStatusDay(!driver.isStatusDay());
                save(driver);
            }
           return driver.isStatusDay();
        } else {
            throw new AuthoryException("invalid token");
        }
    }
}
