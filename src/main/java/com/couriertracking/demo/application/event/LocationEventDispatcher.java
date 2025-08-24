package com.couriertracking.demo.application.event;

import com.couriertracking.demo.application.service.observer.LocationObserver;
import com.couriertracking.demo.domain.model.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
public class LocationEventDispatcher implements LocationEventPublisher{

    private final List<LocationObserver> observers;

    public LocationEventDispatcher(List<LocationObserver> discovered) {
        this.observers = new CopyOnWriteArrayList<>(discovered);
        log.info("LocationEventDispatcher wired with {} listeners.", observers.size());
    }

    @Override
    public void notifyObservers(Long courierId, Location location) {
        log.info("New location event: courierId={} lat={}, lng={}", courierId, location.lat(), location.lng());
        for (LocationObserver observer : observers) {
            try {
                observer.onLocationReceived(courierId, location);
            } catch (Exception ex) {
                log.warn("Observer {} failed for courierId={}: {}", observer.getClass().getSimpleName(), courierId, ex.getMessage());
            }
        }
    }
}
