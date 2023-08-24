package com.example.courier.service;

import com.example.courier.model.Order;
import com.example.courier.repository.OrderRepository;
import com.example.courier.repository.SettingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    SettingService settingService;

    @Autowired
    DriverService driverService;

    static Timer timer;

    public void newOrder(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Order order = objectMapper.readValue(json, Order.class);
        order.setDateStart(new Date());
        order.setStatusDelivery(0);
        orderRepository.save(order);
    }

    public List<Order> getOrderItWork() {
        return orderRepository.findByStatusDeliveryBetween(0, 1);
    }

    public void newTimer(Boolean flag) {
        if (flag) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    driverService.appointmentDriver();
                    settingService.setTimeInDb("none");
                    timer.cancel();
                }
            };
            settingService.setTimeInDb(new SimpleDateFormat("HH:mm").format(new Date()));
            timer.schedule(task, (long) Integer.parseInt( settingService.getValueByKey("timer_sum")) * 60 * 1000);
        } else {
            settingService.setTimeInDb("none");
            timer.cancel();
        }
    }


}
