package com.example.courier.config;

import com.example.courier.service.SettingService;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@PropertySource("classpath:application.properties")
public class RabbitConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    SettingService settingService;

    @Value("${spring.rabbitmq.host}")
    public String rabbitHost;

    @Value("${spring.rabbitmq.port}")
    public int rabbitPort;

    @Value("${spring.rabbitmq.username}")
    public String rabbitUserName;

    @Value("${spring.rabbitmq.password}")
    public String rabbitPassword;

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange("");
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setExchange(userExchange().getName());
        return template;
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitHost);
        connectionFactory.setPort(rabbitPort);
        connectionFactory.setUsername(rabbitUserName);
        connectionFactory.setPassword(rabbitPassword);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

}
