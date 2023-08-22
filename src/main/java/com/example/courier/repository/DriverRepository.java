package com.example.courier.repository;

import com.example.courier.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByLogin(String login);
}
