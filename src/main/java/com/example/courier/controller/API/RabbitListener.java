package com.example.courier.controller.API;

import com.example.courier.model.data.Message;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

@Component
public class RabbitListener {
    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "back")
    public void getMessage(String json){
        Gson gson = new Gson();
        Message message = gson.fromJson(json, Message.class);
        switch (message.getCode()){
            case("accept_order")-> System.out.println(message.getCode());

        }
    }
}
