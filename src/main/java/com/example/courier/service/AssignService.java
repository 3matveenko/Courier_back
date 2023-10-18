package com.example.courier.service;

import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import com.example.courier.repository.AssignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssignService {

    @Autowired
    AssignRepository assignRepository;

    @Autowired
    SettingService settingService;

    /**
     * дисстанция 0,0035 = примерно 400м.
     */
    public static double dist = 0.0035;
    public void checkAssignStatus(Driver _driver){
        double feLat = Double.parseDouble(settingService.getValueByKey("fe_latitude"));
        double feLong = Double.parseDouble(settingService.getValueByKey("fe_longtitude"));
        if( _driver.getLatitude()>(feLat-dist)
                &&_driver.getLatitude()<(feLat+dist)
                &&_driver.getLongitude()>(feLong-dist)
                &&_driver.getLongitude()<(feLong+dist)){

        }
    }
    public void checkAssignStatus(Driver _driver){
        double feLat = Double.parseDouble(settingService.getValueByKey("fe_latitude"));
        double feLong = Double.parseDouble(settingService.getValueByKey("fe_longtitude"));
        if(Math.abs(_driver.getLatitude() - feLat) < dist && Math.abs(_driver.getLongitude() - feLong) < dist){

        }
    }

    public Assign getAssignByOrder(Order _order){
        return assignRepository.findByOrdersContaining(_order);
    }

    public void save(Assign _assign){
        assignRepository.save(_assign);
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
