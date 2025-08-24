package com.couriertracking.demo.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CourierDetail(Long courierId, double lastLat, double lastLng, LocalDateTime lastTime) {
}
