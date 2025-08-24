package com.couriertracking.demo.infrastructure.config;

import com.couriertracking.demo.infrastructure.adapters.jpa.StoreEntity;
import com.couriertracking.demo.infrastructure.adapters.repository.StoreJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreDataLoader implements ApplicationRunner {

    private final StoreJpaRepository storeRepo;
    private final ObjectMapper objectMapper;

    @Value("${bootstrap.stores.enabled:true}")
    private boolean enabled;

    @Value("${bootstrap.stores.resource:classpath:stores.json}")
    private Resource storesResource;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            log.info("Store bootstrap disabled.");
            return;
        }
        if (Objects.isNull(storesResource) || !storesResource.exists()) {
            log.warn("stores.json not found at {}", storesResource);
            return;
        }

        try (InputStream in = storesResource.getInputStream()) {
            List<StoreJson> items = objectMapper.readValue(in, new TypeReference<>() {});
            int inserted = 0, updated = 0;

            for (StoreJson storeJson : items) {
                var opt = storeRepo.findByName(storeJson.name());
                if (opt.isPresent()) {
                    StoreEntity storeEntity = opt.get();
                    if (Double.compare(storeEntity.getLat(), storeJson.lat()) != 0 || Double.compare(storeEntity.getLng(), storeJson.lng()) != 0) {
                        storeEntity.setLat(storeJson.lat());
                        storeEntity.setLng(storeJson.lng());
                        updated++;
                    }
                } else {
                    storeRepo.save(StoreEntity.builder()
                            .name(storeJson.name())
                            .lat(storeJson.lat())
                            .lng(storeJson.lng())
                            .build());
                    inserted++;
                }
            }

            log.info("Store bootstrap done. inserted={}, updated={}", inserted, updated);
        }
    }

    public record StoreJson(String name, double lat, double lng) {}
}
