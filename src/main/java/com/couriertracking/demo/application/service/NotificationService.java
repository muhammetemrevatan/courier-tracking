package com.couriertracking.demo.application.service;

import com.couriertracking.demo.application.service.observer.LocationObserver;
import com.couriertracking.demo.domain.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class NotificationService implements LocationObserver {

    @Override
    public void onLocationReceived(Long courierId, Location location) {
        log.info("[NOTIFICATION] courierId={} time={} lat={}, lng={} -> mock notification sent",
                courierId, location.time(), location.lat(), location.lng());
    }
}
