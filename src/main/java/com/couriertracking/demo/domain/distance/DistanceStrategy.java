package com.couriertracking.demo.domain.distance;

import com.couriertracking.demo.domain.model.Location;

public interface DistanceStrategy {
    double calculateDistanceInMeters(Location from, Location to);
}
