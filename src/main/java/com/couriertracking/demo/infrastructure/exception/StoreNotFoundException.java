package com.couriertracking.demo.infrastructure.exception;

import lombok.Getter;

@Getter
public class StoreNotFoundException extends RuntimeException {
    private final Long storeId;

    public StoreNotFoundException(Long storeId) {
        super("error.store.notfound");
        this.storeId = storeId;
    }

}

