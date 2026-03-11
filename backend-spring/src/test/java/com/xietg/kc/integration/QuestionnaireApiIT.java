package com.xietg.kc.integration;

import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class QuestionnaireApiIT extends AbstractPostgresIT {

	@Autowired
	private WebTestClient webTestClient;

    @Autowired
    private QuestionnaireRepository repository;

    @Test
    void shouldReturnAllQuestionnaires() {
        // Given
        QuestionnaireEntity q = new QuestionnaireEntity();
        q.setId(UUID.randomUUID());
        q.setName("IT Questionnaire");
        q.setVersion(1);
        q.setStatus("DRAFT");

        repository.save(q);

        // When + Then
        webTestClient.get()
                .uri("/api/questionnaires")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body ->
                        assertThat(body).contains("IT Questionnaire")
                );
    }
}