package com.couriertracking.demo.infrastructure.adapters.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "courier_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "courier_details_seq", sequenceName = "courier_details_seq", allocationSize = 1)
public class CourierDetailEntity extends AbstractBaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false, unique = true)
    private CourierEntity courier;

    private Double lastLat;
    private Double lastLng;
    private LocalDateTime lastTime;
}