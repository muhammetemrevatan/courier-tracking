package com.couriertracking.demo.domain.distance;

import com.couriertracking.demo.domain.model.Location;
import org.springframework.stereotype.Component;

@Component
public class HaversineStrategy implements DistanceStrategy {
    private static final double EARTH_RADIUS = 6371000;


    @Override
    public double calculateDistanceInMeters(Location from, Location to) {
        double dLat = Math.toRadians(to.lat() - from.lat());
        double dLon = Math.toRadians(to.lng() - from.lng());

        double haversine = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(from.lat())) * Math.cos(Math.toRadians(to.lat()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double central = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return EARTH_RADIUS * central;
    }
}
