package com.example.courier.service;

import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import com.example.courier.model.RejectOrder;
import com.example.courier.model.data.AssignReject;
import com.example.courier.model.data.Message;
import com.example.courier.model.exception.DriversIsEmptyException;
import com.example.courier.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    SendService sendService;


    static List<Assign> dbAssigns = new ArrayList<>();

    static List<AssignReject> assignRejectList = new ArrayList<>();

    static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setPrettyPrinting()
            .create();

    //********************timer service
    static Timer timerSum;

    static Timer timerNoDriver;

    static Timer rejectTimer;

    public void startTimerSum(){
        if("adaptive".equals(settingService.getValueByKey("order_distribution_principle"))&&"none".equals(settingService.getValueByKey("timer_start_time"))){
            timerSum = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        settingService.setTimerStartTime("none");
                        timerSum.cancel();
                        changeOrders(appointmentDriverAuto());
                    } catch (DriversIsEmptyException e){
                        startNoDriverTimer();
                    }
                }
            };
            settingService.setTimerStartTime(new SimpleDateFormat("HH:mm")
                    .format(new  Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli())));
            timerSum.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum")) * 60 * 1000);
        }
    }



    @Scheduled(cron = "0 0,30 * * * ?")
    public void startTimerSum2(){
        if("schedule".equals(settingService.getValueByKey("order_distribution_principle"))){
            try {
                settingService.setTimerStartTime("none");
                changeOrders(appointmentDriverAuto());
            } catch (DriversIsEmptyException e){
                startNoDriverTimer();
            }
        }
    }

    public void stopTimerSum(){
        if(timerSum!=null){
            settingService.setTimerStartTime("none");
            timerSum.cancel();
        }

    }

    public void startNoDriverTimer(){
        timerNoDriver = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    settingService.setTimerStartTime("none");
                    timerNoDriver.cancel();
                    for(Assign assign: dbAssigns){
                        if(assign.getDriver().isStatusOrder()){
                            assign.getDriver().setTimeFree(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
                            driverService.save(assign.getDriver());
                        }
                    }
                    changeOrders(appointmentDriverAuto());
                } catch (DriversIsEmptyException e){
                    timerNoDriver.cancel();
                    startNoDriverTimer();
                }
            }
        };
        settingService.setTimerStartTime(new SimpleDateFormat("HH:mm").format(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli())));
        timerNoDriver.schedule(task, (long) Integer.parseInt(settingService.getValueByKey("timer_sum_nodriver")) * 60 * 1000);
    }

    public void stopNoDriverTimer(){
        if(timerNoDriver!=null){
            timerNoDriver.cancel();
        }

    }

    public void startRejectTimer(){
        rejectTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                this.cancel();
                changingRejectedOrder();
                //this.run();/////////////
            }
        };
        rejectTimer.schedule(task, (long) 60000);
    }

    public void stopRejectTimer(){
        if(rejectTimer!=null){
            rejectTimer.cancel();
        }
    }

    //********************timer service

    public Order getOrderById(Long _order_id){
        return orderRepository.findById(_order_id).orElseThrow();
    }

    public Long newOrder(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Order order = objectMapper.readValue(json, Order.class);
        order.setDateStart(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
        order.setStatusDelivery(0);
        order.setAngle(locationService.angleBetweenVerticalAndPoint(order.getLatitude(), order.getLongitude()));
        orderRepository.save(order);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String driverToken = jsonObject.get("driver_token").getAsString();
        if(!"".equals(driverToken)){
            giveTheDriverAnOrder(driverToken, order.getId());
        }
        return order.getId();
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
        System.out.println("setTimeResponse = "+new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
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
                dbAssigns.get(i).setTimeStart(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
                dbAssigns.remove(i);
            }
        }
    }

    public void checkingResponseFromDriver(){
        if(!dbAssigns.isEmpty()){
            for (Assign assign: dbAssigns){
                assign.getDriver().setTimeFree(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
                driverService.save(assign.getDriver());
            }
            dbAssigns.clear();
            try{
                changeOrders(appointmentDriverAuto());
            } catch (DriversIsEmptyException e){
                startNoDriverTimer();
            }
        }
    }

    public List<Order> getOrdersStatusProcessingByToken(String drivers_token){
       return orderRepository.findByStatusDeliveryAndDriver(1,driverService.getDriverByToken(drivers_token));
    }
    public void sendOrderStatusProcessingByToken(String token){
        String body = gson.toJson(getOrdersStatusProcessingByToken(token));
        Message newOrders = new Message("","get_my_orders_status_progressing", System.currentTimeMillis(), body);
        rabbitService.sendMessage(token, gson.toJson(newOrders));
    }

    public void changeStatusDeliveryToComplete(Long _id){
        Order order = orderRepository.findById(_id).orElseThrow();
        order.setStatusDelivery(2);
        order.setDateEnd(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
        save(order);
        sendService.sendTo1cAboutCompleteOrder(order.getGuid(), order.getDriver().getToken());
    }

    public void rejectingOrder(Message _message){
        Driver driver = driverService.getDriverByToken(_message.getToken());
        List<Order> orders = getOrdersStatusProcessingByToken(_message.getToken());
        RejectOrder rejectOrder = new RejectOrder(
                _message.getBody(),
                driver,
                new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()),
                driver.getLatitude(),
                driver.getLongitude());
        rejectOrderService.save(rejectOrder);
        for(Order order:orders){
            order.setStatusDelivery(0);
            order.setRejectOrder(rejectOrder);
            save(order);
        }
        assignRejectList.add(new AssignReject(0,_message.getToken(),orders));
        changingRejectedOrder();
    }

    public void changingRejectedOrder(){
            List<Driver> drivers = driverService.getFreeDrivers();
            for (int i = drivers.size()-1; i >=0 ; i--) {
                if(Objects.equals(drivers.get(i).getId(), assignRejectList.get(0).getOrders().get(0).getDriver().getId())){
                    drivers.remove(i);
                }
            }
        if(!assignRejectList.isEmpty()){
            for(AssignReject assignReject: assignRejectList){
                if(drivers.size() <= assignReject.getQueue_id()){
                    assignReject.setQueue_id(0);
                }
                assignReject.setDriver_token(drivers.get(assignReject.getQueue_id()).getToken());
                changeRejectedOrdersByDriverToken(assignReject.getOrders(),drivers.get(assignReject.getQueue_id()).getToken());
                assignReject.setQueue_id(assignReject.getQueue_id()+1);
                startRejectTimer();
            }
        } else {
            startRejectTimer();
        }
    }
    public void changeOrdersByDriverToken(List<Order> _orders, String _driver_token){
        String body = gson.toJson(_orders);
        Message newOrders = new Message("","new_order", System.currentTimeMillis(), body);
        rabbitService.sendMessage(_driver_token,gson.toJson(newOrders));
    }
    public void changeRejectedOrdersByDriverToken(List<Order> _orders, String _driver_token){
        String body = gson.toJson(_orders);
        Message newOrders = new Message("","new_order_rejected", System.currentTimeMillis(), body);
        rabbitService.sendMessage(_driver_token,gson.toJson(newOrders));
    }

    public void acceptRejectedOrder(String _token){
        Driver driver = driverService.getDriverByToken(_token);
        for (int i = assignRejectList.size()-1; i >=0 ; i--) {
            if(Objects.equals(assignRejectList.get(i).getDriver_token(), _token)){
                List<Order> orders = assignRejectList.get(i).getOrders();
                for(Order order:orders){
                    order.setDriver(driver);
                    order.setStatusDelivery(1);
                    save(order);
                }
                assignRejectList.remove(i);
                int a = assignRejectList.size();
            }
        }
        if(assignRejectList.isEmpty()){
            stopRejectTimer();
        }
    }

    public void giveTheDriverAnOrder(String _driverToken, long _orderId){
        for (int i = dbAssigns.size()-1; i >=0 ; i--) {
          boolean flag = false;
          for(Order order:dbAssigns.get(i).getOrders()){
              if(order.getId()==_orderId){
                  flag = true;
              }
          }
          if(flag){
              dbAssigns.remove(i);
          }
        }
        Driver driver = driverService.getDriverByToken(_driverToken);
        List<Order> orders = new ArrayList<>();
        Order order = getOrderById(_orderId);
        order.setStatusDelivery(0);
        order.setDriver(null);
        assignService.deleteOrderFromAssign(order);
        save(order);
        orders.add(order);
        Assign assign = new Assign();
        assign.setDriver(driver);
        assign.setOrders(orders);
        dbAssigns.add(assign);
        String body = gson.toJson(orders);
        Message newOrders = new Message("","new_order", System.currentTimeMillis(), body);
        rabbitService.sendMessage(_driverToken,gson.toJson(newOrders));
    }
}
