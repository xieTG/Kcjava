package com.xietg.kc.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LCFlowIT extends AbstractPostgresIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCreateAndFetchLC() {

        String requestBody = """
            {
              "name": "LC 2026",
              "description": "Integration Test LC",
              "year": 2026
            }
        """;

        // Create LC
        webTestClient.post()
                .uri("/api/lc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk();

        // Fetch all LCs
        webTestClient.get()
                .uri("/api/lc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body ->
                        assertThat(body).contains("LC 2026")
                );
    }
}