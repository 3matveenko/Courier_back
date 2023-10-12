package com.example.courier.service;

import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import com.example.courier.model.data.Message;
import com.example.courier.model.exception.DriversIsEmptyException;
import com.example.courier.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    @Autowired
    RabbitService rabbitService;


    static Timer timer;

    static List<Assign> dbAssigns = new ArrayList<>();

    static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setPrettyPrinting()
            .create();

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
//        rabbitService.deleteExchange("Driver000");
//        rabbitService.deleteQueue("Driver000");
  //      rabbitService.sendMessage("Driver0","Отправленное сообщение");
        return orderRepository.findByStatusDeliveryBetween(0, 1);
    }


    /**
    0- остановить таймер
    1- новый таймер
    2- таймер если водителей не найдено
     */
    public void newTimer(int flag) {
        switch (flag){
            case 0:
                settingService.setTimeInDb("none");
                timer.cancel();
                break;
            case 1:
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            changeOrders(appointmentDriverAuto());
                            settingService.setTimeInDb("none");
                            timer.cancel();
                        } catch (DriversIsEmptyException e){
                            timer.cancel();
                            newTimer(2);
                        }

                    }
                };
                settingService.setTimeInDb(new SimpleDateFormat("HH:mm").format(new Date()));
                timer.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum")) * 60 * 1000);
                break;
            case 2:
                timer = new Timer();
                task = new TimerTask() {
                @Override
                public void run() {
                    try {

                        settingService.setTimeInDb("none");
                        timer.cancel();
                        changeOrders(appointmentDriverAuto());
                    } catch (DriversIsEmptyException e){
                        timer.cancel();
                        newTimer(2);
                    }
                }
                };
                settingService.setTimeInDb(new SimpleDateFormat("HH:mm").format(new Date()));
                timer.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum_nodriver")) * 60 * 1000);
                break;
        }
    }


    public List<Assign> appointmentDriverAuto() throws DriversIsEmptyException {
        int sector = Integer.parseInt(settingService.getValueByKey("angle"));
        List<Order> orders = getOrdersByStatusDelivery(0);
        List<Driver> drivers = getFreeDrivers();
        if (drivers.isEmpty()) {
            throw new DriversIsEmptyException("drivers is empty");
        }
        orders.sort(Comparator.comparing(Order::getAngle));
        drivers.sort(Comparator.comparing(Driver::getTimeFree));

        List<Order> orders1 = new ArrayList<>(orders);
        List<Driver> drivers1 = new ArrayList<>(drivers);

        List<Order> orders2 = new ArrayList<>(orders);
        List<Driver> drivers2 = new ArrayList<>(drivers);

        List<List<Assign>> lists = new ArrayList<>();

        lists.add(assignService.StepByStepPlus(orders, drivers, sector));
        lists.add(assignService.StepByStepMinus(orders1,drivers1, sector));
        lists.add(assignService.getTheBest(orders2, drivers2, sector));

        lists.sort(Comparator.comparing(List<Assign>::size));
        return lists.get(0);
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

    public List<Driver> getFreeDrivers(){
       return driverService.findAllByStatusOrder(true);
    }
    public void changeOrders(List<Assign> assigns){
        List<Driver> drivers = getFreeDrivers();
        for(Assign assign: assigns){
            List<Order> orders;
            dbAssigns.add(new Assign(drivers.get(assigns.indexOf(assign)),assign.getOrders()));
            orders = orderRepository.findByStatusDeliveryAndDriver(1,drivers.get(assigns.indexOf(assign)));
            List<Order> list = assign.getOrders();
            orders.addAll(list);
            String body = gson.toJson(orders);
            Message newOrders = new Message("","new_order", System.currentTimeMillis(), body);
            rabbitService.sendMessage(drivers.get(assigns.indexOf(assign)).getToken(),gson.toJson(newOrders));
        }
        setTimeResponse();
    }

    /**
     * Установка таймера для того, чтобы дождаться ответа от водителей о принятии заказа
     */
    public void setTimeResponse(){
        System.out.println("setTimeResponse = "+new Date());
        Timer timeResponse = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                checkingResponseFromDriver();
            }
        };
        timeResponse.schedule(task,  60000);
    }
    public void acceptOrders(String token){
        Driver driver = driverService.findDriverByToken(token);
        for (int i = dbAssigns.size()-1; i>=0; i--) {
            if(dbAssigns.get(i).getDriver().getId().equals(driver.getId())){
                for (Order order: dbAssigns.get(i).getOrders()){
                    order.setStatusDelivery(1);
                    order.setDriver(driver);
                    save(order);
                }
                dbAssigns.remove(i);
            }
        }
    }

    public void checkingResponseFromDriver(){
        if(!dbAssigns.isEmpty()){
            for (Assign assign: dbAssigns){
                assign.getDriver().setTimeFree(new Date());
                driverService.save(assign.getDriver());
            }
            dbAssigns.clear();
            try{
                changeOrders(appointmentDriverAuto());
            } catch (DriversIsEmptyException e){
                timer.cancel();
                newTimer(2);
            }
        }
    }

    public void getOrderStatusProcessingByToken(String token){
        String body = gson.toJson(orderRepository.findByStatusDeliveryAndDriver(1,driverService.findDriverByToken(token)));
        Message newOrders = new Message("","get_my_orders_status_progressing", System.currentTimeMillis(), body);
        rabbitService.sendMessage(token, gson.toJson(newOrders));
    }
}
