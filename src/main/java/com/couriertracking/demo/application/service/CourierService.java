package com.couriertracking.demo.application.service;

import com.couriertracking.demo.application.port.in.CourierCommandPort;
import com.couriertracking.demo.application.port.in.CourierQueryPort;
import com.couriertracking.demo.application.port.out.*;
import com.couriertracking.demo.domain.model.*;
import com.couriertracking.demo.domain.distance.DistanceStrategy;
import com.couriertracking.demo.domain.policy.ProximityPolicy;
import com.couriertracking.demo.application.service.observer.LocationObserver;
import com.couriertracking.demo.infrastructure.config.RedisConfig;
import com.couriertracking.demo.infrastructure.exception.CourierNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class CourierService implements CourierCommandPort, CourierQueryPort, LocationObserver {

    private final CourierRepositoryPort courierRepo;
    private final StoreRepositoryPort storeRepo;
    private final CourierLogRepositoryPort logRepo;
    private final DistanceStrategy distanceStrategy;
    private final ProximityPolicy proximityPolicy;
    private final CourierStateCachePort stateCache;
    private final RedisConfig.RedisProps redisProps;
    private final CourierDetailRepositoryPort courierDetailRepo;

    @Override
    @Transactional
    public Long createCourier(String name) {
        Courier c = courierRepo.create(name);
        log.info("Courier created id={} name={}", c.getId(), name);
        return c.getId();
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = OptimisticLockException.class,
            backoff = @Backoff(delay = 200, multiplier = 2)
    )
    public void onLocationReceived(Long courierId, Location location) {
        log.info("Processing location event: courierId={} lat={}, lng={}", courierId, location.lat(), location.lng());
        Courier courier = courierRepo.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        Optional<Location> optionalCachedLocation = stateCache.getLastLocation(courierId);
        if (optionalCachedLocation.isPresent()) {
            stateCache.getLastLocation(courierId).ifPresent(courier::setLastLocation);
            log.info("Loaded last location from cache for courierId={}", courierId);
        } else {
            CourierDetail courierDetail = courierDetailRepo.findByCourierDetail(courierId);
            if (!Objects.isNull(courierDetail)) {
                Location dbLocation = new Location(courierDetail.lastLat(), courierDetail.lastLng(), courierDetail.lastTime());
                courier.setLastLocation(dbLocation);
                log.info("Loaded last location from DB for courierId={}", courierId);
            }
        }

        var cachedMap = stateCache.getLastEntryByStore(courierId);
        if (!cachedMap.isEmpty()) {
            courier.getLastEntryByStore().putAll(cachedMap);
            log.info("Loaded last entry map from cache for courierId={}", courierId);
        }

        List<Store> stores = storeRepo.findAll();

        int beforeLogSize = courier.getLogs().size();
        log.info("Before courier travel state courierId={} totalDistance={} logsSize={}", courierId, courier.getTotalDistance(), beforeLogSize);
        courier.updateTravelState(location, distanceStrategy, stores, proximityPolicy);
        int afterLogSize = courier.getLogs().size();
        log.info("Updated courier travel state courierId={} totalDistance={} logsSize={}", courierId, courier.getTotalDistance(), afterLogSize);

        if (afterLogSize > beforeLogSize) {
            log.info("New log entries for courierId={} newLogsSize={}", courierId, afterLogSize - beforeLogSize);
            List<CourierLog> newlyCreated = courier.getLogs().subList(beforeLogSize, afterLogSize);
            newlyCreated.forEach(logRepo::save);
            newlyCreated.forEach(courierLog -> stateCache.putEntryTime(courierId, courierLog.storeId(), courierLog.entryTime()));
        }

        Duration ttl = Optional.ofNullable(redisProps.getTtlSeconds())
                .map(Duration::ofSeconds)
                .orElse(null);

        stateCache.putLastLocation(courierId, location, ttl);

        courierRepo.save(courier);
        log.info("Courier updated id={} totalDistance={} logs={}", courierId, courier.getTotalDistance(), courier.getLogs().size());
    }

    @Recover
    public void recoverFromLockFailure(OptimisticLockException ex, Long courierId, Location location) {
        log.error("Courier {} location update failed permanently after retries.", courierId, ex);
    }

    @Override
    @Transactional(readOnly = true)
    public double getTotalTravelDistance(Long courierId) {
        return courierRepo.findById(courierId).map(Courier::getTotalDistance).orElse(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierLog> getLogs(Long courierId) {
        return logRepo.findByCourierId(courierId);
    }
}