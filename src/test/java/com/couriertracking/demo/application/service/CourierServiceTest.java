package com.couriertracking.demo.application.service;

import com.couriertracking.demo.application.port.out.*;
import com.couriertracking.demo.domain.distance.DistanceStrategy;
import com.couriertracking.demo.domain.model.*;
import com.couriertracking.demo.domain.policy.ProximityPolicy;
import com.couriertracking.demo.infrastructure.config.RedisConfig;
import com.couriertracking.demo.infrastructure.exception.CourierNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.OptimisticLockException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock private CourierRepositoryPort courierRepo;
    @Mock private StoreRepositoryPort storeRepo;
    @Mock private CourierLogRepositoryPort logRepo;
    @Mock private DistanceStrategy distanceStrategy;
    @Mock private CourierStateCachePort stateCache;
    @Mock private RedisConfig.RedisProps redisProps;
    @Mock private CourierDetailRepositoryPort courierDetailRepo;

    private final ProximityPolicy proximityPolicy = new ProximityPolicy(100.0, Duration.ofMinutes(1));

    private CourierService service;

    @BeforeEach
    void setUp() {
        service = new CourierService(
                courierRepo, storeRepo, logRepo, distanceStrategy,
                proximityPolicy, stateCache, redisProps, courierDetailRepo
        );
    }

    @Test
    void createCourier_returnsId() {
        Courier created = Courier.builder().id(42L).build();
        when(courierRepo.create("Emre")).thenReturn(created);

        Long id = service.createCourier("Emre");

        assertEquals(42L, id);
        verify(courierRepo).create("Emre");
    }

    @Nested
    class OnLocationReceivedTests {
        @Test
        void onLocation_withCachedLastLocation_persistsNewLogs_andCaches() {
            Long courierId = 1L;
            Location cached = new Location(40.0, 29.0, LocalDateTime.now().minusMinutes(5));
            Location incoming = new Location(40.1, 29.1, LocalDateTime.now());

            Courier courier = spy(Courier.builder().id(courierId).build());
            when(courierRepo.findById(courierId)).thenReturn(Optional.of(courier));
            when(stateCache.getLastLocation(courierId)).thenReturn(Optional.of(cached)).thenReturn(Optional.of(cached));
            when(stateCache.getLastEntryByStore(courierId)).thenReturn(Collections.emptyMap());
            when(storeRepo.findAll()).thenReturn(Collections.emptyList());
            when(redisProps.getTtlSeconds()).thenReturn(3600L);
            doAnswer(invocation -> {
                Location loc = invocation.getArgument(0);
                courier.getLogs().add(new CourierLog(courierId, 1000L, loc.time()));
                return null;
            }).when(courier).updateTravelState(any(Location.class), any(DistanceStrategy.class), anyList(), any(ProximityPolicy.class));
            when(logRepo.save(any(CourierLog.class))).thenAnswer(inv -> inv.getArgument(0));
            when(courierRepo.save(any(Courier.class))).thenAnswer(inv -> inv.getArgument(0));

            service.onLocationReceived(courierId, incoming);

            assertEquals(cached, courier.getLastLocation());

            ArgumentCaptor<CourierLog> logCaptor = ArgumentCaptor.forClass(CourierLog.class);
            verify(logRepo, times(1)).save(logCaptor.capture());
            CourierLog savedLog = logCaptor.getValue();
            assertEquals(courierId, savedLog.courierId());
            assertEquals(1000L, savedLog.storeId());
            assertEquals(incoming.time(), savedLog.entryTime());

            verify(stateCache).putEntryTime(eq(courierId), eq(1000L), eq(incoming.time()));
            verify(stateCache).putLastLocation(eq(courierId), eq(incoming), eq(Duration.ofSeconds(3600)));
            verify(courierRepo).save(courier);
        }

        @Test
        void onLocation_withoutCache_loadsLastLocationFromDb() {
            Long courierId = 7L;
            Location dbLoc = new Location(41.0, 29.0, LocalDateTime.now().minusMinutes(10));
            Location incoming = new Location(41.01, 29.01, LocalDateTime.now());
            Courier courier = spy(Courier.builder().id(courierId).build());

            when(courierRepo.findById(courierId)).thenReturn(Optional.of(courier));
            when(stateCache.getLastLocation(courierId)).thenReturn(Optional.empty());
            CourierDetail detail = new CourierDetail(courierId, dbLoc.lat(), dbLoc.lng(), dbLoc.time());
            when(courierDetailRepo.findByCourierDetail(courierId)).thenReturn(detail);
            when(stateCache.getLastEntryByStore(courierId)).thenReturn(Collections.emptyMap());
            when(storeRepo.findAll()).thenReturn(Collections.emptyList());
            when(redisProps.getTtlSeconds()).thenReturn(null);
            doAnswer(invocation -> null).when(courier).updateTravelState(any(), any(), anyList(), any());
            when(courierRepo.save(any(Courier.class))).thenAnswer(inv -> inv.getArgument(0));

            service.onLocationReceived(courierId, incoming);

            assertEquals(dbLoc, courier.getLastLocation());

            verify(logRepo, never()).save(any());
            verify(stateCache).putLastLocation(eq(courierId), eq(incoming), isNull());
            verify(courierRepo).save(courier);
        }

        @Test
        void onLocation_courierNotFound_throws() {
            when(courierRepo.findById(999L)).thenReturn(Optional.empty());
            assertThrows(CourierNotFoundException.class,
                    () -> service.onLocationReceived(999L, new Location(40, 29, LocalDateTime.now())));
        }

        @Test
        void recoverMethod_exists() {
            assertDoesNotThrow(() ->
                    service.recoverFromLockFailure(new OptimisticLockException("x"), 1L, new Location(0,0, LocalDateTime.now()))
            );
        }
    }

    @Test
    void getTotalDistance_found() {
        Courier courier = Courier.builder().id(11L).totalDistance(12.5).build();

        when(courierRepo.findById(11L)).thenReturn(Optional.of(courier));

        double d = service.getTotalTravelDistance(11L);
        assertEquals(12.5, d);
    }

    @Test
    void getTotalDistance_notFound() {
        when(courierRepo.findById(88L)).thenReturn(Optional.empty());
        assertEquals(0.0, service.getTotalTravelDistance(88L));
    }

    @Test
    void getLogs_delegatesToRepo() {
        List<CourierLog> logs = List.of(
                new CourierLog(1L, 1000L, LocalDateTime.now()),
                new CourierLog(1L, 1001L, LocalDateTime.now().plusMinutes(1))
        );
        when(logRepo.findByCourierId(1L)).thenReturn(logs);

        List<CourierLog> result = service.getLogs(1L);
        assertEquals(2, result.size());
        assertEquals(1000L, result.get(0).storeId());
        verify(logRepo).findByCourierId(1L);
    }
}