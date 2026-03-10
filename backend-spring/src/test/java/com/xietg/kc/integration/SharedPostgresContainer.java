package com.xietg.kc.integration;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton Postgres container started once per JVM.
 *
 * All tests should reuse SharedPostgresContainer.POSTGRES so we don't start/stop
 * a container per test class (which provoquait le churn de ports et les erreurs Hikari).
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