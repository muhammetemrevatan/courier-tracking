package com.couriertracking.demo.application.event;

import com.couriertracking.demo.domain.model.Location;

public interface LocationEventPublisher {
    void notifyObservers(Long courierId, Location location);
}
