package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.application.port.out.CourierLogRepositoryPort;
import com.couriertracking.demo.domain.model.CourierLog;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierEntity;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierLogEntity;
import com.couriertracking.demo.infrastructure.adapters.jpa.StoreEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierJpaRepository;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierLogJpaRepository;
import com.couriertracking.demo.infrastructure.adapters.repository.StoreJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierLogAdapterTest {

    @Mock private CourierLogJpaRepository logRepo;
    @Mock private CourierJpaRepository courierRepo;
    @Mock private StoreJpaRepository storeRepo;

    private CourierLogRepositoryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new CourierLogAdapter(logRepo, courierRepo, storeRepo);
    }

    @Test
    void findByCourierId_mapsEntitiesToDomain() {
        Long courierId = 1L;
        LocalDateTime entryTime = LocalDateTime.now();

        var courier = CourierEntity.builder().name("Emre").totalDistance(0.0).build();
        var store   = StoreEntity.builder().name("Ataşehir MMM Migros").lat(40.99).lng(29.12).build();
        var entity  = CourierLogEntity.builder().courier(courier).store(store).entryTime(entryTime).build();

        when(logRepo.findByCourier_Id(courierId)).thenReturn(List.of(entity));

        var result = adapter.findByCourierId(courierId);

        assertEquals(1, result.size());
        var log = result.get(0);
        assertEquals(entryTime, log.entryTime());
    }

    @Test
    void save_persistsAndReturnsDomain() {
        Long courierId = 2L;
        Long storeId = 1001L;
        LocalDateTime entryTime = LocalDateTime.now();

        var input = new CourierLog(courierId, storeId, entryTime);

        var courierRef = CourierEntity.builder().name("Ali").totalDistance(0.0).build();
        var store = StoreEntity.builder().name("Ortaköy MMM Migros").lat(41.05).lng(29.02).build();

        when(courierRepo.getReferenceById(courierId)).thenReturn(courierRef);
        when(storeRepo.findById(storeId)).thenReturn(Optional.of(store));

        var savedEntity = CourierLogEntity.builder()
                .courier(courierRef)
                .store(store)
                .entryTime(entryTime)
                .build();
        when(logRepo.save(any(CourierLogEntity.class))).thenReturn(savedEntity);

        var out = adapter.save(input);

        assertEquals(entryTime, out.entryTime());
    }
}