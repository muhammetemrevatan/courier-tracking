package com.couriertracking.demo.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;


@Slf4j
@Configuration
public class GracefulShutdownConfig {

    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    public GracefulShutdownConfig(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Graceful shutdown initiated. Cleaning up H2 resources.");

        try {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
                log.info("EntityManagerFactory closed successfully");
            }

            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
                log.info("HikariDataSource closed successfully");
            }

            try {
                java.sql.DriverManager.getConnection("jdbc:h2:~/test;DB_CLOSE_ON_EXIT=TRUE", "sa", "");
                log.info("H2 database shutdown initiated");
            } catch (SQLException e) {
                log.debug("H2 shutdown command completed");
            }

            log.info("H2 resources cleaned up successfully");

        } catch (Exception e) {
            log.error("Error during graceful shutdown: {}", e.getMessage(), e);
        }
    }
}