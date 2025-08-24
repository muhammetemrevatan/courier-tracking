package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.domain.model.CourierLog;
import com.couriertracking.demo.application.port.out.CourierLogRepositoryPort;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierEntity;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierLogEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierJpaRepository;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierLogJpaRepository;
import com.couriertracking.demo.infrastructure.adapters.repository.StoreJpaRepository;
import com.couriertracking.demo.infrastructure.exception.StoreNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourierLogAdapter implements CourierLogRepositoryPort {

    private final CourierLogJpaRepository logRepo;
    private final CourierJpaRepository courierRepo;
    private final StoreJpaRepository storeRepo;

    @Override
    public List<CourierLog> findByCourierId(Long courierId) {
        return logRepo.findByCourier_Id(courierId)
                .stream()
                .map(e -> new CourierLog(e.getCourier().getId(), e.getStore().getId(), e.getEntryTime()))
                .toList();
    }

    @Override
    public CourierLog save(CourierLog log) {
        CourierEntity courierRef = courierRepo.getReferenceById(log.courierId());

        var store = storeRepo.findById(log.storeId())
                .orElseThrow(() -> new StoreNotFoundException(log.storeId()));

        CourierLogEntity courierLogEntity = CourierLogEntity.builder()
                .store(store)
                .entryTime(log.entryTime())
                .courier(courierRef)
                .build();

        CourierLogEntity saved = logRepo.save(courierLogEntity);
        return new CourierLog(saved.getCourier().getId(), saved.getStore().getId(), saved.getEntryTime());
    }
}
