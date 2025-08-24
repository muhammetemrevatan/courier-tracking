package com.couriertracking.demo.infrastructure.exception;

import lombok.Getter;

@Getter
public class CourierNotFoundException extends RuntimeException {
    private final Long courierId;

    public CourierNotFoundException(Long courierId) {
        super("error.courier.notfound");
        this.courierId = courierId;
    }

}