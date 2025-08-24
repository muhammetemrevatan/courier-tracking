package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.application.port.out.CourierDetailRepositoryPort;
import com.couriertracking.demo.domain.model.CourierDetail;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierDetailEntity;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierDetailJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierDetailAdapterTest {

    @Mock
    private CourierDetailJpaRepository repository;

    private CourierDetailRepositoryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new CourierDetailAdapter(repository);
    }

    @Test
    void findByCourierDetail_mapsEntityToDto_whenPresent() {
        Long courierId = 42L;
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(5);

        var courierEntity = CourierEntity.builder()
                .name("Emre")
                .totalDistance(0.0)
                .build();

        var detailEntity = CourierDetailEntity.builder()
                .courier(courierEntity)
                .lastLat(41.015137)
                .lastLng(28.979530)
                .lastTime(entryTime)
                .build();

        when(repository.findByCourier_Id(courierId)).thenReturn(Optional.of(detailEntity));

        CourierDetail dto = adapter.findByCourierDetail(courierId);

        assertNotNull(dto);
        assertEquals(41.015137, dto.lastLat());
        assertEquals(28.979530, dto.lastLng());
        assertEquals(entryTime, dto.lastTime());
    }

    @Test
    void findByCourierDetail_returnsNull_whenNotFound() {
        Long courierId = 99L;
        when(repository.findByCourier_Id(courierId)).thenReturn(Optional.empty());

        CourierDetail dto = adapter.findByCourierDetail(courierId);

        assertNull(dto);
    }
}