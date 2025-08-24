package com.couriertracking.demo.application.service.observer;

import com.couriertracking.demo.domain.model.Location;

public interface LocationObserver {
    void onLocationReceived(Long courierId, Location location);
}
