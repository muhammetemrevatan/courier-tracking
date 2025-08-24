package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.domain.model.Store;
import com.couriertracking.demo.application.port.out.StoreRepositoryPort;
import com.couriertracking.demo.infrastructure.adapters.repository.StoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreAdapter implements StoreRepositoryPort {

    private final StoreJpaRepository repo;

    @Override
    public List<Store> findAll() {
        return repo.findAll().stream()
                .map(store -> new Store(store.getId(), store.getName(), store.getLat(), store.getLng()))
                .toList();
    }
}