package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.integration.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@Sql(scripts = "/sql/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

@DataJpaTest(properties = {
	    "spring.test.database.replace=NONE",
	    "spring.jpa.hibernate.ddl-auto=none",    // laisser le SQL initialiser le schema
	    "spring.sql.init.mode=always",            // exécute test-schema.sql (classpath:sql/test-schema.sql)
	    "spring.sql.init.schema-locations=classpath:sql/test-schema.sql"
	})
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
