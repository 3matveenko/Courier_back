package com.example.courier.service;

import com.example.courier.model.RejectOrder;
import com.example.courier.repository.RejectOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RejectOrderService {

    @Autowired
    RejectOrderRepository rejectOrderRepository;

    public void save(RejectOrder _rejectOrder){
        rejectOrderRepository.save(_rejectOrder);
    }
}
