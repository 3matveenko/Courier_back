package com.example.courier.service;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    @PostConstruct
    public void init(){
        createExchange("back");
        createDriverQueue("back");
    }

    @Autowired
    public RabbitService(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    public void sendMessage(String token, String message) {
        rabbitTemplate.convertAndSend(token, message);
    }

    public void createExchange(String exchangeName){
        Exchange exchange = new DirectExchange(exchangeName);
        amqpAdmin.declareExchange(exchange);
    }

    public void createDriverQueue(String driverName) {
        Queue queue = new Queue(driverName);
        amqpAdmin.declareQueue(queue);
    }

    public void deleteExchange(String exchangeName) {
        amqpAdmin.deleteExchange(exchangeName);
    }

    public void deleteQueue(String queueName) {
        amqpAdmin.deleteQueue(queueName);
    }
}