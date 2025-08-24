package com.couriertracking.demo.infrastructure.adapters.cache;

import com.couriertracking.demo.application.port.out.CourierStateCachePort;
import com.couriertracking.demo.domain.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourierStateRedisAdapter implements CourierStateCachePort {

    private final RedisTemplate<String, Location> locationRedis;
    private final StringRedisTemplate stringRedis;

    @Override
    public Optional<Location> getLastLocation(Long courierId) {
        return Optional.ofNullable(locationRedis.opsForValue().get(locKey(courierId)));
    }

    @Override
    public void putLastLocation(Long courierId, Location loc, Duration ttl) {
        if (ttl != null)
            locationRedis.opsForValue().set(locKey(courierId), loc, ttl);
        else
            locationRedis.opsForValue().set(locKey(courierId), loc);
    }

    @Override
    public Map<Long, LocalDateTime> getLastEntryByStore(Long courierId) {
        Map<Object, Object> entries = stringRedis.opsForHash().entries(entryKey(courierId));
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.valueOf((String) e.getKey()),
                        e -> LocalDateTime.parse((String) e.getValue())
                ));
    }

    @Override
    public void putEntryTime(Long courierId, Long storeId, LocalDateTime entryTime) {
        stringRedis.opsForHash().put(entryKey(courierId), storeId.toString(), entryTime.toString());
    }


    @Override
    public void putAllEntryTimes(Long courierId, Map<String, LocalDateTime> map) {
        if (map == null || map.isEmpty()) return;
        Map<String, String> raw = map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        stringRedis.opsForHash().putAll(entryKey(courierId), raw);
    }

    @Override
    public void evictAll(Long courierId) {
        locationRedis.delete(locKey(courierId));
        stringRedis.delete(entryKey(courierId));
    }

    private String locKey(Long id)   {
        return "courier:%d:lastLocation".formatted(id);
    }

    private String entryKey(Long id) {
        return "courier:%d:lastEntry".formatted(id);
    }
}
