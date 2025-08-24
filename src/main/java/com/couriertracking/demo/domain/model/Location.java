package com.couriertracking.demo.domain.model;

import java.time.LocalDateTime;

public record Location(double lat, double lng, LocalDateTime time) { }
