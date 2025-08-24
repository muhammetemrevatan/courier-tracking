package com.couriertracking.demo.infrastructure.adapters.repository;

import com.couriertracking.demo.infrastructure.adapters.jpa.CourierLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierLogJpaRepository extends JpaRepository<CourierLogEntity, Long> {
    List<CourierLogEntity> findByCourier_Id(Long courierId);
}
