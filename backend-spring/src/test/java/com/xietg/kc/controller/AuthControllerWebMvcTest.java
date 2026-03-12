package com.xietg.kc.controller;

import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.entity.UserRole;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.security.AuthService;
import com.xietg.kc.security.JwtService;
import com.xietg.kc.security.PasswordService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.xietg.kc.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, properties = "app.cors-origins=http://localhost:3000")
@EnableConfigurationProperties(AppProperties.class)
class AuthControllerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordService passwordService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    AuthService authService;

    @Test
    @DisplayName("Login returns JWT token and role for valid credentials")
    void login_should_return_token_and_role() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("admin@example.com");
        user.setPasswordHash("hashed");
        user.setRole(UserRole.admin);

        when(userRepository.findByEmail(eq("admin@example.com"))).thenReturn(Optional.of(user));
        when(passwordService.verifyPassword("secret", "hashed")).thenReturn(true);
        when(jwtService.createAccessToken("admin@example.com", "admin", 3600)).thenReturn("jwt-token");

        String body = """
            {
              "email": "admin@example.com",
              "password": "secret"
            }
            """;

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.access_token").value("jwt-token"))
           .andExpect(jsonPath("$.role").value("admin"));
    }
    @Test
    @DisplayName("Returns 409 Conflict when registering an email that already exists")
    void register_should_return_conflict_when_user_already_exists() throws Exception {
        UserEntity existingUser = new UserEntity();
        existingUser.setEmail("admin@example.com");
        existingUser.setPasswordHash("hashed");
        existingUser.setRole(UserRole.admin);

        when(userRepository.findByEmail(eq("admin@example.com")))
                .thenReturn(Optional.of(existingUser));

        String body = """
            {
              "email": "admin@example.com",
              "password": "secret"
            }
            """;

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isConflict());

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordService, never()).hashPassword(any());
    }
}