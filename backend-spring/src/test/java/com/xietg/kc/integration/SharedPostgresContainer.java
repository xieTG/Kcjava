package com.xietg.kc.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.*;

/**
 * Singleton Postgres container started once per JVM.
 * It creates the enum types (if missing) via JDBC, then executes the SQL schema (tables).
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

        // Create enum types (if not exists) using JDBC to avoid ScriptUtils/dollar-quote parsing issues.
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement st = conn.createStatement()) {

            // user_role
            try (ResultSet rs = st.executeQuery(
                    "SELECT 1 FROM pg_type WHERE typname = 'user_role'")) {
                if (!rs.next()) {
                    st.execute("CREATE TYPE user_role AS ENUM ('user','admin')");
                }
            }

            // submission_status
            try (ResultSet rs = st.executeQuery(
                    "SELECT 1 FROM pg_type WHERE typname = 'submission_status'")) {
                if (!rs.next()) {
                    st.execute("CREATE TYPE submission_status AS ENUM (" +
                            "'finalized','parse_error','parsed_ok','received','scored','scoring_in_progress')");
                }
            }

            // Now execute the rest of the schema (tables etc.) from the SQL file.
            // The SQL file must *not* contain the CREATE TYPE statements anymore.
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/test-schema.sql"));

        } catch (Exception e) {
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

    private SharedPostgresContainer() {}
}