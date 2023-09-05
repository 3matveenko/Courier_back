package com.example.courier.service;

import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import com.example.courier.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    LocationService locationService;

    @Autowired
    DriverService driverService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    SettingService settingService;


    static Timer timer;

    public void newOrder(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Order order = objectMapper.readValue(json, Order.class);
        order.setDateStart(new Date());
        order.setStatusDelivery(0);
        order.setAngle(locationService.angleBetweenVerticalAndPoint(order.getLatitude(), order.getLongitude()));
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
                    appointmentDriverAuto();
                    settingService.setTimeInDb("none");
                    timer.cancel();
                }
            };
            settingService.setTimeInDb(new SimpleDateFormat("HH:mm").format(new Date()));
            timer.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum")) * 60 * 1000);
        } else {
            settingService.setTimeInDb("none");
            timer.cancel();
        }
    }

    /**
     * 0-нет водителей для распределения
     * 1-все заказы распределены
     * @return
     */
    public int appointmentDriverAuto() {
        int angle = Integer.parseInt(settingService.getValueByKey("angle"));
        List<Order> orders = getOrdersByStatusDelivery(0);
        List<Driver> drivers = driverService.findAllByStatusOrder(true);
        if (drivers.isEmpty()) {
            return 0;
        }
        orders.sort(Comparator.comparing(Order::getAngle));
        drivers.sort(Comparator.comparing(Driver::getTimeFree));
        do {
            Driver driver = drivers.get(0);
            driver.setStatusOrder(false);
            driverService.save(driver);
            boolean flag = true;
            int x = 1;
            int y = orders.size() - 1;
            if (drivers.size() > 1) {
                while (flag) {
                    if (orders.get(0).getAngle() + angle > orders.get(x).getAngle()) {
                        x = x + 1;
                    } else if (orders.get(0).getAngle() - angle < 0) {
                        if (orders.get(y).getAngle() > (360 + orders.get(0).getAngle() - angle)) {
                            y = y - 1;
                        } else {
                            flag = false;
                        }
                    } else {
                        flag = false;
                    }
                }
                orders.get(0).setDriver(driver);
                if (x != 1) {
                    for (int i = 1; i <= x; i++) {
                        orders.get(i).setDriver(driver);
                    }
                }
                if (y != orders.size() - 1) {
                    for (int i = y; i < orders.size(); i++) {
                        orders.get(i).setDriver(driver);
                    }
                }
                //***************************************
                //отправь уведомление водителю, дождались ответа
                //***************************************
                Iterator<Order> iterator = orders.iterator();
                while (iterator.hasNext()) {
                    Order order = iterator.next();
                    if (order.getDriver() != null) {
                        order.setStatusDelivery(1);
                        iterator.remove();
                    }
                }
            } else {
                for (Order order : orders) {
                    order.setStatusDelivery(1);
                    order.setDriver(driver);
                    save(order);
                    //*****************
                    //уведомление водиле. Дождались ответа
                    //*****************
                    driver.setStatusOrder(false);
                    driverService.save(driver);
                }
            }
            drivers.remove(driver);
        } while (!orders.isEmpty());
        return 1;
    }

    public List<Order> getOrderByDate(Date date1) {
        Date date2 = new Date(date1.getTime());
        date2.setHours(23);
        return orderRepository.findByDateStartBetween(date1, date2);
    }

    /**
     * 0 - принят
     * 1 - в пути
     * 2 - доставлен
     */
    public List<Order> getOrdersByStatusDelivery(Integer status) {
        return orderRepository.findByStatusDelivery(status);
    }

    public void save(Order order) {
        orderRepository.save(order);
    }


}
