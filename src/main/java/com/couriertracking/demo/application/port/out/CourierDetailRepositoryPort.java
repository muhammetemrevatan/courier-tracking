package com.couriertracking.demo.application.port.out;

import com.couriertracking.demo.domain.model.CourierDetail;

public interface CourierDetailRepositoryPort {
    CourierDetail findByCourierDetail(Long courierId);
}
