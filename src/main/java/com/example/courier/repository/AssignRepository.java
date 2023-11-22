package com.example.courier.repository;


import com.example.courier.model.Assign;
import com.example.courier.model.Driver;
import com.example.courier.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface AssignRepository extends JpaRepository<Assign, Long> {
    public Assign findByOrdersContaining(Order order);

    public List<Assign> findByDriverAndTimeEnd(Driver driver, Date timeEnd);

    List<Assign> findByTimeStartBetween(Date date1,Date date2);

    List<Assign> findByTimeEndBefore(Date date);
}
