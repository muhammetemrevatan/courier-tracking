package com.couriertracking.demo.infrastructure.adapters.repository;

import com.couriertracking.demo.infrastructure.adapters.jpa.CourierEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierJpaRepository extends JpaRepository<CourierEntity, Long> {

    @EntityGraph(attributePaths = {"logs", "detail"})
    Optional<CourierEntity> findWithAllById(Long id);
}
