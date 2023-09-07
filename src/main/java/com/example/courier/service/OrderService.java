package com.example.courier.service;

import com.example.courier.model.Assign;
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

    @Autowired
    AssignService assignService;


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
        int sector = Integer.parseInt(settingService.getValueByKey("angle"));
        List<Order> orders = getOrdersByStatusDelivery(0);
        List<Driver> drivers = driverService.findAllByStatusOrder(true);
        if (drivers.isEmpty()) {
            return 0;
        }
        orders.sort(Comparator.comparing(Order::getAngle));
        drivers.sort(Comparator.comparing(Driver::getTimeFree));
        List<Order> orders1 = new ArrayList<>(orders);
        List<Driver> drivers1 = new ArrayList<>(drivers);
        List<Assign> assigns =  assignService.StepByStepPlus(orders, drivers, sector);
        for(Assign assign: assigns){
            System.out.println("driver = " + assign.getDriver().getId());
            for(Order order: assign.getOrders()){
                System.out.println("order angle = "+order.getAngle());
            }
        }
        System.out.println("**********************");
        List<Assign> assigns2 =  assignService.StepByStepMinus(orders1, drivers1, sector);
        for(Assign assign: assigns2){
            System.out.println("driver = " + assign.getDriver().getId());
            for(Order order: assign.getOrders()){
                System.out.println("order angle = "+order.getAngle());
            }
        }
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
