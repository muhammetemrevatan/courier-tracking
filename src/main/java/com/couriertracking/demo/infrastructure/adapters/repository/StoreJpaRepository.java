package com.couriertracking.demo.infrastructure.adapters.repository;

import com.couriertracking.demo.infrastructure.adapters.jpa.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreJpaRepository extends JpaRepository<StoreEntity, Long> {
    Optional<StoreEntity> findByName(String name);
}
