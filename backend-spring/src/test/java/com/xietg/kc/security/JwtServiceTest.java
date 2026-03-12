package com.xietg.kc.security;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setJwtSecret("unit-test-secret");
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("Creates a valid JWT token and decodes its subject and role")
    void should_create_and_decode_valid_token() {
        String token = jwtService.createAccessToken("admin@example.com", "admin", 3600);

        JwtService.JwtPayload payload = jwtService.decodeToken(token);

        assertEquals("admin@example.com", payload.subject());
        assertEquals("admin", payload.role());
    }

    @Test
    @DisplayName("Rejects an expired JWT token")
    void should_reject_expired_token() throws InterruptedException {
        String token = jwtService.createAccessToken("admin@example.com", "admin", 1);

        Thread.sleep(1200);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> jwtService.decodeToken(token)
        );

        assertEquals("Token expired", ex.getMessage());
        assertEquals(401, ex.getStatus().value());
    }

    @Test
    @DisplayName("Rejects an invalid JWT token format")
    void should_reject_invalid_token() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> jwtService.decodeToken("this-is-not-a-jwt")
        );

        assertEquals("Invalid token", ex.getMessage());
        assertEquals(401, ex.getStatus().value());
    }
}