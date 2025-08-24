package com.couriertracking.demo.infrastructure.adapters.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@BatchSize(size = 10)
@SequenceGenerator(name = "couriers_seq", sequenceName = "couriers_seq", allocationSize = 1)
public class CourierEntity extends AbstractBaseEntity {

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double totalDistance;

    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourierLogEntity> logs = new ArrayList<>();

    @OneToOne(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CourierDetailEntity detail;
}