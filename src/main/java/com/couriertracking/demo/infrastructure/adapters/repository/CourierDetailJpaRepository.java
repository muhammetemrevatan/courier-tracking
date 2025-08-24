package com.couriertracking.demo.infrastructure.adapters.repository;

import com.couriertracking.demo.infrastructure.adapters.jpa.CourierDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierDetailJpaRepository extends JpaRepository<CourierDetailEntity, Long> {

    Optional<CourierDetailEntity> findByCourier_Id(Long courierId);
}
