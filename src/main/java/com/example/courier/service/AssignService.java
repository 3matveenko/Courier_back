package com.example.courier.service;

import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssignService {

    public List<Assign> StepByStepPlus(List<Order> orders, List<Driver> drivers,int sector){
        List<Assign> listAssign = new ArrayList<>();
        do {
            if(orders.isEmpty()){//почему while не отработал?
                break;
            }
            Driver driver = drivers.get(0);
            List<Order> ordersForDriver = new ArrayList<>();
            if (drivers.size() > 1) {
                boolean bool = true;
                double secondPoint = orders.get(0).getAngle()+ sector;
                if (secondPoint>360){
                    bool = false;
                }
                List<Integer> indexes = new ArrayList<>();
                for (int i=0;i<orders.size();i++){
                    if(bool){
                        if (orders.get(i).getAngle()<=secondPoint){
                            indexes.add(i);
                        }
                    } else {
                            indexes.add(i);
                    }
                }
                secondPoint = orders.get(indexes.get(indexes.size()-1)).getAngle();
                double firstPoint = secondPoint - sector;
                if (firstPoint<0){
                    firstPoint = 360+firstPoint;
                    bool = false;
                }
                indexes = new ArrayList<>();
                for (int i=0;i<orders.size();i++){
                    if(bool){
                        if(orders.get(i).getAngle()>=firstPoint&&orders.get(i).getAngle()<=secondPoint){
                            indexes.add(i);
                        }
                    } else {
                        if(orders.get(i).getAngle()>=firstPoint||orders.get(i).getAngle()<=secondPoint){
                            indexes.add(i);
                        }
                    }

                }
                if (!indexes.isEmpty()) {
                for (int i = orders.size()-1; i >=0 ; i--) {
                        for(Integer index: indexes){
                            if(i==index){
                                ordersForDriver.add(orders.get(i));
                                orders.remove(orders.get(i));
                            }
                        }
                    }
                }
            } else {
                ordersForDriver.addAll(orders);
                orders.clear();
            }
            drivers.remove(driver);
            listAssign.add(new Assign(driver,ordersForDriver));
        } while (!orders.isEmpty());
        return listAssign;
    }

    public List<Assign> StepByStepMinus(List<Order> orders, List<Driver> drivers,int sector){
        List<Assign> listAssign = new ArrayList<>();
        do {
            if(orders.isEmpty()){
                break;
            }
            Driver driver = drivers.get(0);
            List<Order> ordersForDriver = new ArrayList<>();
            if (drivers.size() > 1) {
                boolean bool = true;
                double firstPoint = orders.get(orders.size()-1).getAngle()- sector;
                if (firstPoint<0){
                    bool = false;
                }
                List<Integer> indexes = new ArrayList<>();
                for (int i=orders.size()-1;i>=0;i--){
                    if(bool){
                        if (orders.get(i).getAngle()>=firstPoint){
                            indexes.add(i);
                        }
                    } else {
                            indexes.add(i);
                    }
                }
                firstPoint = orders.get(indexes.get(indexes.size()-1)).getAngle();
                double secondPoint = firstPoint + sector;
                if (secondPoint>360){
                    secondPoint = secondPoint-360;
                    bool = false;
                }
                indexes = new ArrayList<>();
                for (int i=0;i<orders.size();i++){
                    if(bool){
                        if(orders.get(i).getAngle()>=firstPoint&&orders.get(i).getAngle()<=secondPoint){
                            indexes.add(i);
                        }
                    } else {
                        if(orders.get(i).getAngle()>=firstPoint||orders.get(i).getAngle()<=secondPoint){
                            indexes.add(i);
                        }
                    }

                }
                if (!indexes.isEmpty()) {
                    for (int i = orders.size()-1; i >=0 ; i--) {
                        for(Integer index: indexes){
                            if(i==index){
                                ordersForDriver.add(orders.get(i));
                                orders.remove(orders.get(i));
                            }
                        }
                    }
                }
            } else {
                ordersForDriver.addAll(orders);
                orders.clear();
            }
            drivers.remove(driver);
            listAssign.add(new Assign(driver,ordersForDriver));
        } while (!orders.isEmpty());
        return listAssign;
    }

    public List<Assign> getTheBest(List<Order> orders, List<Driver> drivers,int sector){
        for (int i = sector; i <= 360 ; i++) {
            for(Order order: orders){
                if(order.getAngle()<=i&&order.getAngle()>=i-sector){

                }
            }
        }
    }
}
