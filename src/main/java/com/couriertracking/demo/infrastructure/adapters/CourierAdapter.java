package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.domain.model.Courier;
import com.couriertracking.demo.application.port.out.CourierRepositoryPort;
import com.couriertracking.demo.domain.model.CourierLog;
import com.couriertracking.demo.domain.model.Location;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierDetailEntity;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierEntity;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierLogEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierJpaRepository;
import com.couriertracking.demo.infrastructure.exception.CourierNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourierAdapter implements CourierRepositoryPort {

    private final CourierJpaRepository repository;

    @Override
    public Optional<Courier> findById(Long courierId) {
        return repository.findWithAllById(courierId).map(this::toDomain);
    }

    @Override
    public Courier save(Courier courier) {
        CourierEntity entity = repository.findById(courier.getId())
                .orElseThrow(() -> new CourierNotFoundException(courier.getId()));

        entity.setTotalDistance(courier.getTotalDistance());

        if (courier.getLastLocation() != null) {
            CourierDetailEntity detail = entity.getDetail();
            if (detail == null) {
                detail = CourierDetailEntity.builder().courier(entity).build();
                entity.setDetail(detail);
            }
            detail.setLastLat(courier.getLastLocation().lat());
            detail.setLastLng(courier.getLastLocation().lng());
            detail.setLastTime(courier.getLastLocation().time());
        }

        CourierEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Courier create(String name) {
        CourierEntity courierEntity = CourierEntity.builder()
                .name(name)
                .totalDistance(0.0)
                .build();

        CourierEntity savedCourierEntity = repository.save(courierEntity);

        return Courier.builder()
                .id(savedCourierEntity.getId())
                .totalDistance(savedCourierEntity.getTotalDistance())
                .build();
    }

    private Courier toDomain(CourierEntity courierEntity) {
        Courier courier = Courier.builder()
                .id(courierEntity.getId())
                .totalDistance(courierEntity.getTotalDistance())
                .build();

        if (courierEntity.getDetail() != null &&
                courierEntity.getDetail().getLastLat() != null &&
                courierEntity.getDetail().getLastLng() != null &&
                courierEntity.getDetail().getLastTime() != null) {
            courier.setLastLocation(new Location(
                    courierEntity.getDetail().getLastLat(),
                    courierEntity.getDetail().getLastLng(),
                    courierEntity.getDetail().getLastTime()
            ));
        }

        List<CourierLogEntity> logEntities = courierEntity.getLogs();
        if (logEntities != null && !logEntities.isEmpty()) {
            logEntities.stream()
                    .sorted(Comparator.comparing(CourierLogEntity::getEntryTime))
                    .forEach(courierLogEntity -> {
                        courier.getLogs().add(new CourierLog(courierEntity.getId(), courierLogEntity.getStore().getId(), courierLogEntity.getEntryTime()));
                        courier.getLastEntryByStore().merge(courierLogEntity.getStore().getId(), courierLogEntity.getEntryTime(), (oldTime, newTime) -> oldTime.isBefore(newTime) ? newTime : oldTime
                        );
                    });
        }

        return courier;
    }
}