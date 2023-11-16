package com.example.courier.service;

import com.example.courier.model.Driver;
import com.example.courier.model.Order;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

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

    @Autowired
    SendService sendService;

    @Scheduled(cron = "0 0 23 * * ?")
    public void setDiversStatus() {
        Date monthAgo = new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli());
        monthAgo.setMonth(monthAgo.getMonth()-1);
        List<Driver> drivers = driverRepository.findAll();
        for (Driver driver: drivers){
            driver.setStatusOrder(false);
            driver.setStatusDay(false);
            if(driver.getLastActivity().before(monthAgo)&& !Objects.equals(driver.getLogin(), "")){
                deleteById(driver.getId());
            }
        }
    }

    public List<Driver> getFreeDrivers(){
        List<Driver> drivers = driverRepository.findAllByStatusOrderOrderByTimeFree(true);
        drivers.sort(Comparator.comparing(Driver::getTimeFree).reversed());
        return drivers;
    }

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
            driver.setLastActivity(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
            driverRepository.save(driver);
            sendService.sendTo1cAboutCreatingNewDriver(driver);
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
        List<Driver> drivers =  driverRepository.findAll();
        drivers.sort(Comparator.comparing(Driver::isStatusOrder).reversed());
        return drivers;
    }


    public void deleteById(Long driverId){
        Driver driver = driverRepository.findById(driverId).orElseThrow();
        driver.logout();
        driver.setLogin("");
        driver.setPassword("");
        rabbitService.deleteQueue(driver.getToken());
        save(driver);
    }

    public void save(Driver driver){
        driverRepository.save(driver);
    }

    public Driver getDriverByToken(String token){
        return driverRepository.findByToken(token).orElseThrow();
    }
    public void getLocation(Message message){
        Gson gson = new Gson();
        Location location = gson.fromJson(message.getBody(), Location.class);
        Driver driver = getDriverByToken(message.getToken());
        driver.setLastUpdateLocation(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
        driver.setLatitude(location.getLatitude());
        driver.setLongitude(location.getLongitude());
        assignService.checkAssignStatus(driver);
        save(driver);
    }


    public Boolean getStatusOrderByToken(String token, Boolean flag) throws AuthoryException{
        Optional<Driver> optional = driverRepository.findByToken(token);
        if(optional.isPresent()){
            Driver driver = optional.get();
            if(flag){
                if(driver.isStatusOrder()){
                    driver.setTimeFree(null);
                    driver.setStatusOrder(false);
                } else {
                    Date date = new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli());
                    driver.setTimeFree(date);
                    driver.setLastActivity(date);
                    driver.setStatusOrder(true);
                    driver.setStatusDay(true);
                }
                save(driver);
            }
           return driver.isStatusOrder();
        } else {
            throw new AuthoryException("invalid token");
        }
    }
    public void changeDriverOrderStatus(Long driverId) throws AuthoryException {
        Driver driver = driverRepository.findById(driverId).orElseThrow();
        getStatusOrderByToken(driver.getToken(),true);
        driverRepository.save(driver);
    }

    public void setDeliveryStatusOrderFalseByToken(String _token){
        Driver driver = getDriverByToken(_token);
        driver.setTimeFree(null);
        driver.setStatusOrder(false);
        save(driver);
    }

    public void logout(String _token){
       Driver driver = getDriverByToken(_token);
       driver.logout();
       save(driver);
    }
}
