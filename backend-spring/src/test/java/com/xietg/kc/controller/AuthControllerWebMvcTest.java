package com.xietg.kc.controller;

import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.entity.UserRole;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.security.JwtService;
import com.xietg.kc.security.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordService passwordService;

    @MockitoBean
    JwtService jwtService;

    @Test
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
}
