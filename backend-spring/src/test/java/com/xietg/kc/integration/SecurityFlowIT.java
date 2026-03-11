package com.xietg.kc.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityFlowIT extends AbstractPostgresIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldRejectAccessWithoutToken() {

        webTestClient.get()
                .uri("/api/lc")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void shouldRegisterLoginAndAccessProtectedEndpoint() throws Exception {

        // language=json
        String registerBody = """
            {
              "email": "it@test.com",
              "password": "Password123!"
            }
        """;

        // 1️⃣ Register
        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerBody)
                .exchange()
                .expectStatus().isOk();

        // 2️⃣ Login
        ObjectMapper mapper = new ObjectMapper();

        String loginResponse = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        Map<String, String> json = mapper.readValue(loginResponse, Map.class);
        String token = json.get("token");

        assertThat(token).isNotBlank();

        // 3️⃣ Access protected endpoint with token
        webTestClient.get()
                .uri("/api/lc")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }
}