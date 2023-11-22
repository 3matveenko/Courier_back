package com.example.courier.service;

import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import com.example.courier.model.data.Message;
import com.example.courier.repository.AssignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AssignService {

    @Autowired
    AssignRepository assignRepository;

    @Autowired
    SettingService settingService;


    /**
     * дисстанция 0.0007 = примерно 78m по широте. и 60m по долготе
     * в одном градусе широты 111км
     * в одном градусе долготы 85км
     */
    public static double dist = 0.0014;


    @Scheduled(cron = "0 0 3 * * ?")
    public void autoCloseAssigns() {
        for(Assign assign : assignRepository.findByTimeEndBefore(new  Date(1L))){
            assign.setTimeEnd(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
            for (int i = assign.getOrders().size()-1; i >=0 ; i--) {
                if(assign.getOrders().get(i).getStatusDelivery()==1){
                    assign.getOrders().remove(i);
                }
            }
            if(assign.getOrders().isEmpty()){
                delete(assign);
            } else {
                save(assign);
            }

        }
    }
    public void deleteOrderFromAssign(Order _order){
        Assign assign = getAssignByOrder(_order);
        if(assign!=null){
            assign.getOrders().remove(_order);
            if(assign.getOrders().isEmpty()){
                delete(assign);
            } else {
                save(assign);
            }
        }
    }

    public void delete(Assign assign){
        assignRepository.delete(assign);
    }

    public void checkAssignStatus(Driver _driver){
        double feLat = Double.parseDouble(settingService.getValueByKey("fe_latitude"));
        double feLong = Double.parseDouble(settingService.getValueByKey("fe_longtitude"));
        if(Math.abs(_driver.getLatitude() - feLat) < dist && Math.abs(_driver.getLongitude() - feLong) < dist){
            List<Assign> assigns = getAssignByDriver(_driver);
            boolean flag = true;
            for (Assign assign:assigns){
                for (Order order: assign.getOrders()){
                    if(order.getStatusDelivery()==1){
                        flag = false;
                    }
                }
                if(flag){
                    assign.setTimeEnd(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
                    save(assign);
                }
            }
        }
    }

    public List<Assign> getAssignByDriver(Driver driver){
        return assignRepository.findByDriverAndTimeEnd(driver, new Date(0L));
    }
    public Assign getAssignByOrder(Order _order){
        return assignRepository.findByOrdersContaining(_order);
    }

    public void save(Assign _assign){
        assignRepository.save(_assign);
    }

    public void craeteNewAssign(List<Order> _orders, Driver _driver){
        List<Assign> assigns = getAssignByDriver(_driver);
        if(assigns.isEmpty()){
            Assign assign = new Assign();
            assign.setTimeStart(new Date(ZonedDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneId.of("UTC")).toInstant().toEpochMilli()));
            assign.setDriver(_driver);
            assign.setOrders(_orders);
            save(assign);
        } else {
            Assign assign = assigns.get(assigns.size()-1);
            assign.getOrders().clear();
            for (Order order: _orders){
                assign.getOrders().add(order);
            }
            save(assign);
        }
    }

    public List<Assign> getAssignsByDate(Date date1) {
        Date date2 = new Date(date1.getTime());
        date2.setHours(23);
        List<Assign> assigns = assignRepository.findByTimeStartBetween(date1, date2);
        for (Assign assign:assigns){
            assign.getOrders().sort(Comparator.comparing(Order::getDateEnd));
        }
        return assigns;
    }

    public List<Assign> StepByStepPlus(List<Order> _orders, List<Driver> drivers, int sector) {
        List<Assign> listAssign = new ArrayList<>();
        while (!_orders.isEmpty()) {
            Driver driver = drivers.get(0);
            List<Order> ordersForDriver = new ArrayList<>();
            if (drivers.size() > 1) {
                boolean bool = true;
                double secondPoint = _orders.get(0).getAngle() + sector;
                if (secondPoint > 360) {
                    bool = false;
                }
                List<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < _orders.size(); i++) {
                    if (bool) {
                        if (_orders.get(i).getAngle() <= secondPoint) {
                            indexes.add(i);
                        }
                    } else {
                        indexes.add(i);
                    }
                }
                secondPoint = _orders.get(indexes.get(indexes.size() - 1)).getAngle();
                double firstPoint = secondPoint - sector;
                if (firstPoint < 0) {
                    firstPoint = 360 + firstPoint;
                    bool = false;
                }
                indexes.clear();
                kkk(_orders, ordersForDriver, bool, secondPoint, indexes, firstPoint);
            } else {
                ordersForDriver.addAll(_orders);
                _orders.clear();
            }
            drivers.remove(driver);
            Assign assign = new Assign();
            assign.setDriver(driver);
            assign.setOrders(ordersForDriver);
            listAssign.add(assign);
        }
        return listAssign;
    }

    private void kkk(List<Order> orders, List<Order> ordersForDriver, boolean bool, double secondPoint, List<Integer> indexes, double firstPoint) {
        for (int i = 0; i < orders.size(); i++) {
            if (bool) {
                if (orders.get(i).getAngle() >= firstPoint && orders.get(i).getAngle() <= secondPoint) {
                    indexes.add(i);
                }
            } else {
                if (orders.get(i).getAngle() >= firstPoint || orders.get(i).getAngle() <= secondPoint) {
                    indexes.add(i);
                }
            }
        }
        if (!indexes.isEmpty()) {
            for (int i = orders.size() - 1; i >= 0; i--) {
                for (Integer index : indexes) {
                    if (i == index) {
                        ordersForDriver.add(orders.get(i));
                        orders.remove(orders.get(i));
                    }
                }
            }
        }
    }

    public List<Assign> StepByStepMinus(List<Order> orders, List<Driver> drivers, int sector) {
        List<Assign> listAssign = new ArrayList<>();
        do {
            if (orders.isEmpty()) {
                break;
            }
            Driver driver = drivers.get(0);
            List<Order> ordersForDriver = new ArrayList<>();
            if (drivers.size() > 1) {
                boolean bool = true;
                double firstPoint = orders.get(orders.size() - 1).getAngle() - sector;
                if (firstPoint < 0) {
                    bool = false;
                }
                List<Integer> indexes = new ArrayList<>();
                for (int i = orders.size() - 1; i >= 0; i--) {
                    if (bool) {
                        if (orders.get(i).getAngle() >= firstPoint) {
                            indexes.add(i);
                        }
                    } else {
                        indexes.add(i);
                    }
                }
                firstPoint = orders.get(indexes.get(indexes.size() - 1)).getAngle();
                double secondPoint = firstPoint + sector;
                if (secondPoint > 360) {
                    secondPoint = secondPoint - 360;
                    bool = false;
                }
                indexes.clear();
                kkk(orders, ordersForDriver, bool, secondPoint, indexes, firstPoint);
            } else {
                ordersForDriver.addAll(orders);
                orders.clear();
            }
            drivers.remove(driver);
            Assign assign = new Assign();
            assign.setDriver(driver);
            assign.setOrders(ordersForDriver);
            listAssign.add(assign);
        } while (!orders.isEmpty());
        return listAssign;
    }

    public List<Assign> getTheBest(List<Order> orders, List<Driver> drivers, int sector) {
        List<Assign> listAssign = new ArrayList<>();
        while (!orders.isEmpty()) {
            int count = 0;
            int first = 0;
            List<Order> ordersForDriver = new ArrayList<>();
            for (int i = sector; i < 360 + sector; i++) {
                List<Order> orders1 = new ArrayList<>();

                for (Order order : orders) {
                    if (i <= 360) {
                        if (order.getAngle() <= i && order.getAngle() >= i - sector) {
                            orders1.add(order);
                        }
                    } else {
                        if ((order.getAngle() <= (i - 360)) || (order.getAngle() >= (i - sector))) {
                            orders1.add(order);
                        }
                    }

                }
                if (count < orders1.size()) {
                    ordersForDriver = new ArrayList<>(orders1);
                    count = orders1.size();
                    first = i - sector;
                }
            }

            if (drivers.size() > 1) {
                Assign assign = new Assign();
                assign.setDriver(drivers.get(0));
                drivers.remove(drivers.get(0));
                assign.setOrders(ordersForDriver);

                listAssign.add(assign);
            } else {
                Assign assign = new Assign();
                assign.setDriver(drivers.get(0));
                assign.setOrders(orders);
                listAssign.add(assign);
                return listAssign;
            }
            for (Order order : ordersForDriver) {
                orders.remove(order);
            }
        }
        return listAssign;
    }
}
