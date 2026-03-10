package com.xietg.kc.integration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Postgres container started once per JVM.
 * It creates the enum types (if missing) via JDBC, then executes the SQL schema (tables).
 *
 * This class uses a custom SQL script executor that correctly handles:
 * - single/double quotes
 * - dollar-quoted strings ($tag$...$tag$)
 * - -- line comments and /* ... *\/ block comments
 * and splits on semicolons only when outside of quotes/comments.
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

        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement st = conn.createStatement()) {

            // 1) ensure enums exist via safe JDBC
            try (ResultSet rs = st.executeQuery(
                    "SELECT 1 FROM pg_type WHERE typname = 'user_role'")) {
                if (!rs.next()) {
                    st.execute("CREATE TYPE user_role AS ENUM ('user','admin')");
                }
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT 1 FROM pg_type WHERE typname = 'submission_status'")) {
                if (!rs.next()) {
                    st.execute("CREATE TYPE submission_status AS ENUM (" +
                            "'finalized','parse_error','parsed_ok','received','scored','scoring_in_progress')");
                }
            }

            // 2) execute SQL script from classpath in a robust way
            executeSqlScript(conn, "sql/test-schema.sql");

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

    // --- Helper: read classpath resource and execute statements robustly ---
    private static void executeSqlScript(Connection conn, String classpathResource) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathResource);
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            String sql = sb.toString();
            List<String> statements = splitSqlStatements(sql);

            try (Statement st = conn.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (trimmed.isEmpty()) continue;
                    st.execute(trimmed);
                }
            }
        }
    }

    /**
     * Splits SQL into statements delimited by semicolons that are not inside
     * single/double quotes, dollar-quoted strings, or comments.
     */
    private static List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean inSingle = false;
        boolean inDouble = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inDollar = false;
        String dollarTag = null;

        int len = sql.length();
        for (int i = 0; i < len; i++) {
            char c = sql.charAt(i);
            char next = (i + 1 < len) ? sql.charAt(i + 1) : '\0';

            // handle line comment --
            if (!inSingle && !inDouble && !inBlockComment && !inDollar && !inLineComment && c == '-' && next == '-') {
                inLineComment = true;
                cur.append(c);
                continue;
            }
            if (inLineComment) {
                cur.append(c);
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }

            // block comment start /*
            if (!inSingle && !inDouble && !inBlockComment && !inDollar && c == '/' && next == '*') {
                inBlockComment = true;
                cur.append(c);
                continue;
            }
            if (inBlockComment) {
                cur.append(c);
                if (c == '*' && next == '/') {
                    cur.append(next);
                    i++;
                    inBlockComment = false;
                }
                continue;
            }

            // dollar-quote start?
            if (!inSingle && !inDouble && !inDollar && c == '$') {
                int j = i + 1;
                while (j < len && sql.charAt(j) != '$' && (Character.isLetterOrDigit(sql.charAt(j)) || sql.charAt(j) == '_')) {
                    j++;
                }
                if (j < len && sql.charAt(j) == '$') {
                    dollarTag = sql.substring(i, j + 1); // e.g. $$ or $tag$
                    inDollar = true;
                    cur.append(dollarTag);
                    i = j;
                    continue;
                }
            } else if (inDollar) {
                if (c == '$' && dollarTag != null) {
                    int end = i + dollarTag.length();
                    if (end <= len && sql.regionMatches(i, dollarTag, 0, dollarTag.length())) {
                        cur.append(dollarTag);
                        i += dollarTag.length() - 1;
                        inDollar = false;
                        dollarTag = null;
                        continue;
                    }
                }
                cur.append(c);
                continue;
            }

            // quotes handling
            if (!inDouble && c == '\'' && !inSingle) {
                inSingle = true;
                cur.append(c);
                continue;
            } else if (inSingle && c == '\'') {
                cur.append(c);
                if (i + 1 < len && sql.charAt(i + 1) == '\'') {
                    // escaped quote ''
                    cur.append('\'');
                    i++;
                    continue;
                } else {
                    inSingle = false;
                }
                continue;
            }
            if (!inSingle && c == '"' && !inDouble) {
                inDouble = true;
                cur.append(c);
                continue;
            } else if (inDouble && c == '"') {
                cur.append(c);
                inDouble = false;
                continue;
            }

            // top-level semicolon marks end of statement
            if (!inSingle && !inDouble && !inDollar && !inBlockComment && !inLineComment && c == ';') {
                statements.add(cur.toString());
                cur.setLength(0);
                continue;
            }

            cur.append(c);
        }

        String last = cur.toString().trim();
        if (!last.isEmpty()) {
            statements.add(last);
        }
        return statements;
    }
}