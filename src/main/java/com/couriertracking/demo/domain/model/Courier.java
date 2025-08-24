package com.couriertracking.demo.domain.model;

import com.couriertracking.demo.domain.distance.DistanceStrategy;
import com.couriertracking.demo.domain.policy.ProximityPolicy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder(toBuilder = true)
public class Courier {
    private final Long id;

    @Setter
    private Location lastLocation;

    @Builder.Default
    private double totalDistance = 0.0;

    @Builder.Default
    private final List<CourierLog> logs = new ArrayList<>();

    @Builder.Default
    private final Map<Long, LocalDateTime> lastEntryByStore = new HashMap<>();

    public void updateTravelState(Location courierCurrentLocation, DistanceStrategy strategy, List<Store> stores, ProximityPolicy policy) {
        if (lastLocation != null) {
            totalDistance += strategy.calculateDistanceInMeters(lastLocation, courierCurrentLocation);
        }
        lastLocation = courierCurrentLocation;

        stores.forEach(store -> {
            Location storeLocation = new Location(store.lat(), store.lng(), null);
            double distanceFromCourierToStore = strategy.calculateDistanceInMeters(courierCurrentLocation, storeLocation);

            if (distanceFromCourierToStore <= policy.radiusMeters()) {
                LocalDateTime lastEntry = lastEntryByStore.get(store.id());

                boolean recentlyEntered = lastEntry != null && lastEntry.plus(policy.reentryThreshold()).isAfter(courierCurrentLocation.time());
                if (!recentlyEntered) {
                    CourierLog entry = new CourierLog(id, store.id(), courierCurrentLocation.time());
                    logs.add(entry);
                    lastEntryByStore.put(store.id(), entry.entryTime());
                }
            }
        });
    }
}