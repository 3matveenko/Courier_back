package com.example.courier.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * crm_token - токен для запросов от 1с
 * timer_sum - интервал распреления заказов
 * timer_start_time - время поступления первого заказа (или начала работы в зависимости от выбранного принципа распределения заказов)
 * angle - Диапазон раздачи заказов для одного водителя
 * timer_sum_nodriver - время перераспределения заказов в случае отсутствия свободных водителей
 * fe_latitude - Координаты базовой точки(широта)
 * fe_longtitude - Координаты базовой точки(долгота)
 * protocol - http/https
 * server_name - ip бэка
 * server_port - порт бэка
 * back_queue_name - имя очереди бэка, в раббите
 * rabbit_server_name - iз раббита
 * rabbit_server_port - порт раббита
 * rabbit_username - имя пользователя в раббите
 * rabbit_password - пароль раббита
 * order_distribution_principle - Принцип распределения заказов:
 * 1) по расписанию - отсчет таймера распределения начинается с началом рабочего дня
 * 2) адаптивный - отсчет таймера распределения начинается с послуплением 0 заказа
 * begining_work - начало рабочего дня
 */

@Getter
@Setter
@Entity
@Table(name="t_settings")
public class Setting{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "this_key")
    private String key;

    @Column(name = "value")
    private String value;
}

