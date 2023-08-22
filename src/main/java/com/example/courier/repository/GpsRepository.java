package com.example.courier.repository;

import com.example.courier.model.Gps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface GpsRepository extends JpaRepository<Gps,Long> {
}
