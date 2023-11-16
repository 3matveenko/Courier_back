package com.example.courier.controller.API;

import com.example.courier.model.data.Message;
import com.example.courier.service.*;
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

    @Autowired
    RabbitService rabbitService;

    @Autowired
    AssignService assignService;

    @Autowired
    SendService sendService;

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "back")
    public void getMessage(String json) {
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);
        try {
            switch (message.getCode()) {
                case ("accept_order") -> {
                    if (message.getMillisecondsSinceEpoch() + 60000 > System.currentTimeMillis()) {
                        orderService.acceptOrders(message.getToken());
                        driverService.setDeliveryStatusOrderFalseByToken(message.getToken());
                        assignService.craeteNewAssign(orderService.getOrdersStatusProcessingByToken(message.getToken()), driverService.getDriverByToken(message.getToken()));
                    }
                }
                case ("location") -> driverService.getLocation(message);
                case ("get_my_orders_status_progressing") -> {
                    Thread.sleep(500);
                    orderService.sendOrderStatusProcessingByToken(message.getToken());
                }

                case ("order_success") -> {
                    orderService.changeStatusDeliveryToComplete(Long.parseLong(message.getBody()));
                    orderService.sendOrderStatusProcessingByToken(message.getToken());
                }
                case ("reject_order")-> {
                    orderService.rejectingOrder(message);
                }
                case ("accept_rejected_order")->{
                    orderService.acceptRejectedOrder(message.getToken());
                    driverService.setDeliveryStatusOrderFalseByToken(message.getToken());
                }
                case ("logout")->driverService.logout(message.getToken());
                case ("send_sms")->sendService.sendSms(message);

            }
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
