package com.couriertracking.demo.application.port.out;

import com.couriertracking.demo.domain.model.Store;

import java.util.List;

public interface StoreRepositoryPort {
    List<Store> findAll();
}
