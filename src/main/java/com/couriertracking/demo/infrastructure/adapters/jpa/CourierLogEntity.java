package com.couriertracking.demo.infrastructure.adapters.jpa;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "courier_logs")
@Builder
@SequenceGenerator(name = "courier_logs_seq", sequenceName = "courier_logs_seq", allocationSize = 1)
public class CourierLogEntity extends AbstractBaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "courier_id", nullable = false)
    private CourierEntity courier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;
}