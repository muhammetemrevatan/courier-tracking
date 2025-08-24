package com.couriertracking.demo.infrastructure.config;

import com.couriertracking.demo.domain.policy.ProximityPolicy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CourierConfig {

    @Bean
    @ConfigurationProperties(prefix = "courier")
    public CourierProps courierProps() {
        return new CourierProps();
    }

    @Bean
    public ProximityPolicy proximityPolicy(CourierProps p) {
        return new ProximityPolicy(p.getStoreRadius(), Duration.ofMinutes(p.getReentryThresholdMinutes()));
    }

    @Setter
    @Getter
    public static class CourierProps {
        private double storeRadius;
        private int reentryThresholdMinutes;
    }
}
