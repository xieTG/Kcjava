package com.xietg.kc.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Singleton Postgres container started once per JVM.
 * It also runs src/test/resources/sql/test-schema.sql once, right after the container starts,
 * so the schema is initialised exactly once by the test JVM.
 */
public final class SharedPostgresContainer {

    public static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("kc_test")
            .withUsername("kc")
            .withPassword("kc");

    static {
        // Start once for the whole JVM so tests reuse the same container:
        POSTGRES.start();

        // Execute the test schema once (safe because the SQL file is idempotent).
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            // ClassPathResource assumes the file is on test classpath: src/test/resources/sql/test-schema.sql
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test-schema.sql"));
        } catch (Exception e) {
            // If schema init fails here, fail fast so test run stops with a clear error.
            throw new RuntimeException("Failed to initialise test schema on SharedPostgresContainer", e);
        }

        // Ensure it stops on JVM shutdown (best-effort).
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (POSTGRES.isRunning()) {
                    POSTGRES.stop();
                }
            } catch (Exception ignored) {
            }
        }));
    }

    // Prevent instantiation
    private SharedPostgresContainer() {}
}