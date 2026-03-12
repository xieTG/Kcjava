package com.xietg.kc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Rejects access to protected endpoints when no token is provided")

    void shouldRejectAccessWithoutToken() throws Exception {

        mockMvc.perform(get("/lcs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registers, logs in, and accesses a protected endpoint with a valid JWT")

    void shouldRegisterLoginAndAccessProtectedEndpoint() throws Exception {
    	
    	ObjectMapper objectMapper = new ObjectMapper();

        String body = """
            {
              "email": "it@test.com",
              "password": "Password123!"
            }
        """;

        // 1️⃣ Register
        
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // 2️⃣ Login
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


		@SuppressWarnings("unchecked")
		Map<String, String> json =
                objectMapper.readValue(loginResponse, Map.class);

        String token = json.get("access_token");

        assertThat(token).isNotBlank();

        // 3️⃣ Access protected endpoint
        mockMvc.perform(get("/lcs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

    }
}