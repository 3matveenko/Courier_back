package com.example.courier.repository;

import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface OrderRepository extends JpaRepository<Order,Long> {

    Optional<Order> findByGuid (String guid);

    List<Order> findByStatusDeliveryBetween(Integer a, Integer b);

    List<Order> findByDateStartBetween(Date date1,Date date2);

    List<Order> findByStatusDelivery(Integer status);

    List<Order> findByStatusDeliveryAndDriver(Integer status, Driver driver);


}
