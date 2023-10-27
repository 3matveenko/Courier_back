package com.example.courier.repository;

import com.example.courier.model.RejectOrder;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface RejectOrderRepository extends JpaRepository<RejectOrder, Long> {

}
