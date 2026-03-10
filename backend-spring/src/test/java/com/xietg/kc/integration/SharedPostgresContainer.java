package com.xietg.kc.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import com.github.dockerjava.api.command.CreateContainerCmd;

/**
 * Singleton Postgres container started once per JVM.
 * Made reusable and given a fixed name so Testcontainers will reuse it instead of creating many.
 */
public final class SharedPostgresContainer {

    public static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("kc_test")
            .withUsername("kc")
            .withPassword("kc")
            // ask Testcontainers to reuse the container between runs (requires ~/.testcontainers.properties)
            .withReuse(true)
            // give it a deterministic container name
            .withCreateContainerCmdModifier((CreateContainerCmd cmd) -> cmd.withName("kc_test_shared_container"));

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

    private SharedPostgresContainer() {}
}