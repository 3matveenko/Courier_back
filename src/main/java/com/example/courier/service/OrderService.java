package com.example.courier.service;

import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import com.example.courier.model.RejectOrder;
import com.example.courier.model.data.DriversQueuePlusOrder;
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

    @Autowired
    RejectOrderService rejectOrderService;

    @Autowired
    TimerStarter timerStarter;

    static List<Assign> dbAssigns = new ArrayList<>();

    static List<DriversQueuePlusOrder> rejectedOrders = new ArrayList<>();

    static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setPrettyPrinting()
            .create();

    public Order getOrderById(Long _order_id){
        return orderRepository.findById(_order_id).orElseThrow();
    }

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


    public List<Assign> appointmentDriverAuto() throws DriversIsEmptyException {
        int sector = Integer.parseInt(settingService.getValueByKey("angle"));
        List<Order> orders = getOrdersByStatusDelivery(0);
        List<Driver> drivers = driverService.getFreeDrivers();
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


    public void changeOrders(List<Assign> assigns){
        List<Driver> drivers = driverService.getFreeDrivers();
        for(Assign assign: assigns){
            List<Order> orders;
            Assign assignNew = new Assign();
            assignNew.setDriver(drivers.get(assigns.indexOf(assign)));
            assignNew.setOrders(assign.getOrders());
            dbAssigns.add(assignNew);
            orders = orderRepository.findByStatusDeliveryAndDriver(1,drivers.get(assigns.indexOf(assign)));
            List<Order> list = assign.getOrders();
            orders.addAll(list);
            changeOrdersByDriverToken(orders, drivers.get(assigns.indexOf(assign)).getToken());
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
        Driver driver = driverService.getDriverByToken(token);
        for (int i = dbAssigns.size()-1; i>=0; i--) {
            if(dbAssigns.get(i).getDriver().getId().equals(driver.getId())){
                for (Order order: dbAssigns.get(i).getOrders()){
                    order.setStatusDelivery(1);
                    order.setDriver(driver);
                    save(order);
                }
                dbAssigns.get(i).setTimeStart(new Date());
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
                timerStarter.startNoDriverTimer();
            }
        }
    }

    public List<Order> getOrderStatusProcessingByToken(String drivers_token){
       return orderRepository.findByStatusDeliveryAndDriver(1,driverService.getDriverByToken(drivers_token));
    }

    public void changeStatusDeliveryToComplete(Long _id){
        Order order = orderRepository.findById(_id).orElseThrow();
        order.setStatusDelivery(2);
        order.setDateEnd(new Date());
        save(order);
    }

    public void rejectingOrder(Message _message){
        Driver driver = driverService.getDriverByToken(_message.getToken());
        List<Order> orders = getOrderStatusProcessingByToken(_message.getToken());
        RejectOrder rejectOrder = new RejectOrder(_message.getBody(),orders,driver,new Date(),driver.getLatitude(),driver.getLongitude());
        rejectOrderService.save(rejectOrder);
        for(Order order:orders){
            order.setStatusDelivery(0);
            order.setRejectOrder(rejectOrder);
            save(order);
        }
        rejectedOrders.add(new DriversQueuePlusOrder(0,orders));
        changingRejectedOrder();
    }

    public void changingRejectedOrder(){
        if(!rejectedOrders.isEmpty()){
            for(DriversQueuePlusOrder driversQueuePlusOrder:rejectedOrders){
                List<Driver> drivers = driverService.getFreeDrivers();
                drivers.remove(driversQueuePlusOrder.getOrders().get(0).getDriver());
                drivers.sort(Comparator.comparing(Driver::getTimeFree));
                int queue = Math.min(drivers.size() - 1, driversQueuePlusOrder.getQueue_id());
                changeOrdersByDriverToken(driversQueuePlusOrder.getOrders(),drivers.get(queue).getToken());
                driversQueuePlusOrder.setQueue_id(queue+1);

            }
        }
    }
    public void changeOrdersByDriverToken(List<Order> _orders, String _driver_token){
        String body = gson.toJson(_orders);
        Message newOrders = new Message("","new_order", System.currentTimeMillis(), body);
        rabbitService.sendMessage(_driver_token,gson.toJson(newOrders));
    }
}
