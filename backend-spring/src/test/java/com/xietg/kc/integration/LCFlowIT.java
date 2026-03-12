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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class LCFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Creates a leadership compass entry and retrieves it with a valid JWT")

    void shouldCreateAndFetchLC() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String authBody = """
            {
              "email": "it-lc-lc-flow@test.com",
              "password": "Password123!"
            }
        """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        @SuppressWarnings("unchecked")
        Map<String, String> json = objectMapper.readValue(loginResponse, Map.class);

        String token = json.get("access_token");

        String requestBody = """
            {
              "name": "LC 2026",
              "description": "Integration Test LC",
              "year": 2026
            }
        """;

        mockMvc.perform(post("/lcs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        String body = mockMvc.perform(get("/lcs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("LC 2026");
    }
}