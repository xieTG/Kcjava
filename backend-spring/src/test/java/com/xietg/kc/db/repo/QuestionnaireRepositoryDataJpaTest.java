package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.integration.AbstractPostgresIT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@Sql(scripts = "/sql/test-schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(properties = {
    "spring.test.database.replace=NONE",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:sql/test-schema.sql"
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class QuestionnaireRepositoryDataJpaTest extends AbstractPostgresIT {

    @Autowired
    QuestionnaireRepository questionnaireRepository;

    @Test
    void findByStatus_should_return_only_matching_questionnaires() {
        QuestionnaireEntity q1 = buildQuestionnaire("Questionnaire A", 1, "published", "template-a");
        QuestionnaireEntity q2 = buildQuestionnaire("Questionnaire B", 1, "draft", "template-b");
        QuestionnaireEntity q3 = buildQuestionnaire("Questionnaire C", 2, "published", "template-c");

        questionnaireRepository.saveAll(List.of(q1, q2, q3));
        questionnaireRepository.flush();

        List<QuestionnaireEntity> result = questionnaireRepository.findByStatus("published");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(q ->
                "Questionnaire A".equals(q.getName()) || "Questionnaire C".equals(q.getName())
        ));
    }

    @Test
    void findByStatus_should_return_empty_list_when_none_exists() {
        List<QuestionnaireEntity> result = questionnaireRepository.findByStatus("non-existent-status");
        assertTrue(result.isEmpty());
    }

    private QuestionnaireEntity buildQuestionnaire(String name, Integer version, String status, String templateKey) {
        QuestionnaireEntity q = new QuestionnaireEntity();
        q.setName(name);
        q.setVersion(version);
        q.setStatus(status);
        q.setTemplateFileKey(templateKey);
        // id left null so @PrePersist will generate it
        return q;
    }
}
