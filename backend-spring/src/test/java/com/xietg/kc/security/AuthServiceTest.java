package com.xietg.kc.security;

import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        authService = new AuthService(jwtService, userRepository);
    }

    @Test
    @DisplayName("Returns the user associated with a valid Bearer token")
    void should_return_user_for_valid_bearer_token() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@example.com");

        when(jwtService.decodeToken("good-token"))
                .thenReturn(new JwtService.JwtPayload("Admin@Example.com", "admin"));
        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(user));

        UserEntity result = authService.requireUser("Bearer good-token");

        assertSame(user, result);
        verify(jwtService).decodeToken("good-token");
        verify(userRepository).findByEmail("admin@example.com");
    }

    @Test
    @DisplayName("Rejects authentication when the Authorization header is missing")
    void should_reject_missing_header() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.requireUser(null)
        );

        assertEquals("Missing token", ex.getMessage());
        assertEquals(401, ex.getStatus().value());
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    @DisplayName("Rejects authentication when the Authorization header is not Bearer")
void should_reject_non_bearer_header() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.requireUser("Basic abc123")
        );

        assertEquals("Missing token", ex.getMessage());
        assertEquals(401, ex.getStatus().value());
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    @DisplayName("Rejects authentication when the JWT subject is blank")
void should_reject_blank_subject_in_token() {
        when(jwtService.decodeToken("bad-token"))
                .thenReturn(new JwtService.JwtPayload("   ", "admin"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.requireUser("Bearer bad-token")
        );

        assertEquals("Invalid token", ex.getMessage());
        assertEquals(401, ex.getStatus().value());
        verify(jwtService).decodeToken("bad-token");
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Rejects authentication when the JWT user does not exist in the database")
void should_reject_unknown_user() {
        when(jwtService.decodeToken("good-token"))
                .thenReturn(new JwtService.JwtPayload("user@example.com", "user"));
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.requireUser("Bearer good-token")
        );

        assertEquals("Unknown user", ex.getMessage());
        assertEquals(401, ex.getStatus().value());
    }
}