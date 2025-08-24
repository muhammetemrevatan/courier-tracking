package com.couriertracking.demo.infrastructure.adapters;

import com.couriertracking.demo.application.port.out.CourierDetailRepositoryPort;
import com.couriertracking.demo.domain.model.CourierDetail;
import com.couriertracking.demo.infrastructure.adapters.jpa.CourierDetailEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.CourierDetailJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CourierDetailAdapter implements CourierDetailRepositoryPort {

    private final CourierDetailJpaRepository repository;

    @Override
    public CourierDetail findByCourierDetail(Long courierId) {
        CourierDetailEntity entity = repository.findByCourier_Id(courierId).orElse(null);
        return Objects.isNull(entity) ? null : toDto(entity);
    }

    private CourierDetail toDto(CourierDetailEntity entity) {
        return CourierDetail.builder()
                .courierId(entity.getCourier().getId())
                .lastLat(entity.getLastLat())
                .lastLng(entity.getLastLng())
                .lastTime(entity.getLastTime())
                .build();
    }
}
