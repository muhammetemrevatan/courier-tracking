package com.couriertracking.demo.infrastructure.adapters.jpa;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores",uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "stores_seq", sequenceName = "stores_seq", allocationSize = 1)
public class StoreEntity extends AbstractBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;
}