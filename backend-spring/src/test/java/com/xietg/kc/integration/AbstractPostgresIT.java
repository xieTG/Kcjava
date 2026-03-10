package com.xietg.kc.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for DB integration tests.
 *
 * It registers the JDBC properties from the singleton SharedPostgresContainer so all tests
 * share the same Postgres instance and the same JDBC URL across the whole JVM.
 *
 * Tests that need DB should 'extends AbstractPostgresIT'.
 */
@ActiveProfiles("test")
public abstract class AbstractPostgresIT {

    @DynamicPropertySource
    static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> SharedPostgresContainer.POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.username", () -> SharedPostgresContainer.POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> SharedPostgresContainer.POSTGRES.getPassword());
    }
}