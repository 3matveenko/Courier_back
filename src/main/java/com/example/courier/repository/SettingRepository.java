package com.example.courier.repository;

import com.example.courier.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface SettingRepository extends JpaRepository<Setting, Long> {

    Optional<Setting> findByKey(String key);
}
