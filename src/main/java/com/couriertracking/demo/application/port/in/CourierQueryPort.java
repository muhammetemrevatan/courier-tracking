package com.couriertracking.demo.application.port.in;

import com.couriertracking.demo.domain.model.CourierLog;

import java.util.List;

public interface CourierQueryPort {
    double getTotalTravelDistance(Long courierId);
    List<CourierLog> getLogs(Long courierId);
}
