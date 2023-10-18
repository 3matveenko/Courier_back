package com.example.courier.repository;


import com.example.courier.model.Assign;
import com.example.courier.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AssignRepository extends JpaRepository<Assign, Long> {
    public Assign findByOrdersContaining(Order order);
}