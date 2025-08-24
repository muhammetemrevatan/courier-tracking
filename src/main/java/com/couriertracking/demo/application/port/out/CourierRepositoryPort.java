package com.couriertracking.demo.application.port.out;

import com.couriertracking.demo.domain.model.Courier;

import java.util.Optional;

public interface CourierRepositoryPort {
    Optional<Courier> findById(Long courierId);
    Courier save(Courier courier);
    Courier create(String name);
}
