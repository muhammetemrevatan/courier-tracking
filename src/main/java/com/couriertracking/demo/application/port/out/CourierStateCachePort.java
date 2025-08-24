package com.couriertracking.demo.application.port.out;

import com.couriertracking.demo.domain.model.Location;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface CourierStateCachePort {
    Optional<Location> getLastLocation(Long courierId);
    void putLastLocation(Long courierId, Location loc, Duration ttl);

    Map<Long, LocalDateTime> getLastEntryByStore(Long courierId);
    void putEntryTime(Long courierId, Long  storeId, LocalDateTime entryTime);
    void putAllEntryTimes(Long courierId, Map<String, LocalDateTime> map);

    void evictAll(Long courierId);
}