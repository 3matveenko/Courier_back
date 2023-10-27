package com.example.courier.controller.API;

import com.example.courier.model.data.Message;
import com.example.courier.service.DriverService;
import com.example.courier.service.OrderService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.NoSuchElementException;

@Component
public class RabbitListener {

    @Autowired
    DriverService driverService;

    @Autowired
    OrderService orderService;

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "back")
    public void getMessage(String json) {
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);
        try {
            switch (message.getCode()) {
                case ("accept_order") -> {
                    if (message.getMillisecondsSinceEpoch() + 60000 > System.currentTimeMillis()) {
                        orderService.acceptOrders(message.getToken());
                    }
                }
                case ("location") -> driverService.getLocation(message);
                case ("get_my_orders_status_progressing") -> {
                    orderService.getOrderStatusProcessingByToken(message.getToken());
                }

                case ("order_success") -> {
                    orderService.changeStatusDeliveryToComplete(Long.parseLong(message.getBody()));
                    orderService.getOrderStatusProcessingByToken(message.getToken());
                }
                case ("reject_order")->{
                    orderService.
                }
            }
        } catch (NoSuchElementException e){

        }
    }
}
