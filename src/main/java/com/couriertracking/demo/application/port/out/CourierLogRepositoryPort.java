package com.couriertracking.demo.application.port.out;

import com.couriertracking.demo.domain.model.CourierLog;

import java.util.List;

public interface CourierLogRepositoryPort {
    List<CourierLog> findByCourierId(Long courierId);
    CourierLog save(CourierLog log);
}
