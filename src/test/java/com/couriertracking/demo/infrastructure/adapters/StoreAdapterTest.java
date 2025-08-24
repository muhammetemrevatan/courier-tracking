package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.application.port.out.StoreRepositoryPort;
import com.couriertracking.demo.domain.model.Store;
import com.couriertracking.demo.infrastructure.adapters.jpa.StoreEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.StoreJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class StoreAdapterTest {

    @Mock
    private StoreJpaRepository repo;

    @Test
    void findAll_mapsEntitiesToDomain() {
        var entity1 = StoreEntity.builder()
                .name("Ataşehir MMM Migros")
                .lat(40.9923307)
                .lng(29.1244229)
                .build();
        var entity2 = StoreEntity.builder()
                .name("Ortaköy MMM Migros")
                .lat(41.055783)
                .lng(29.0210292)
                .build();
        when(repo.findAll()).thenReturn(List.of(entity1, entity2));

        StoreRepositoryPort adapter = new StoreAdapter(repo);

        List<Store> result = adapter.findAll();

        assertEquals(2, result.size());
        Store store1 = result.get(0);
        Store store2 = result.get(1);

        assertEquals("Ataşehir MMM Migros", store1.name());
        assertEquals(40.9923307, store1.lat());
        assertEquals(29.1244229, store1.lng());

        assertEquals("Ortaköy MMM Migros", store2.name());
        assertEquals(41.055783, store2.lat());
        assertEquals(29.0210292, store2.lng());
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(repo.findAll()).thenReturn(List.of());
        StoreRepositoryPort adapter = new StoreAdapter(repo);

        List<Store> result = adapter.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}