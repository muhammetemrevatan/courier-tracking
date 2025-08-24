package com.couriertracking.demo.domain.policy;

import java.time.Duration;

public record ProximityPolicy(double radiusMeters, Duration reentryThreshold) { }
