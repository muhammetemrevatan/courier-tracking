package com.couriertracking.demo.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CourierLog(Long courierId, Long storeId, LocalDateTime entryTime) {}
