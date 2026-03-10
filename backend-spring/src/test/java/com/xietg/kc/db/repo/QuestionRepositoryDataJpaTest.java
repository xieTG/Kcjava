package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.QuestionEntity;
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
//@Sql(scripts = "/sql/test-schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@SpringBootTest(properties = {
    "spring.test.database.replace=NONE",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:sql/test-schema.sql"
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class QuestionRepositoryDataJpaTest extends AbstractPostgresIT {

    @Autowired
    QuestionRepository questionRepository;

    @Test
    void findAllByQuestionnaireId_should_return_only_matching_questions() {
        UUID questionnaire1 = UUID.randomUUID();
        UUID questionnaire2 = UUID.randomUUID();

        QuestionEntity q1 = buildQuestion(questionnaire1, "Standard", "Identity", "1", "Question A", "text", "Help A");
        QuestionEntity q2 = buildQuestion(questionnaire1, "Technical", "Security", "2", "Question B", "text", "Help B");
        QuestionEntity q3 = buildQuestion(questionnaire2, "Standard", "Ops", "1", "Question C", "text", "Help C");

        questionRepository.saveAll(List.of(q1, q2, q3));
        questionRepository.flush();

        List<QuestionEntity> result = questionRepository.findAllByQuestionnaireId(questionnaire1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(q -> q.getText().equals("Question A") || q.getText().equals("Question B")));
    }

    @Test
    void findDistinctQuestionTabsByQuestionnaireId_should_return_sorted_distinct_tabs() {
        UUID questionnaireId = UUID.randomUUID();

        QuestionEntity q1 = buildQuestion(questionnaireId, "Technical", "Security", "1", "Question A", "text", "Help A");
        QuestionEntity q2 = buildQuestion(questionnaireId, "Standard", "Identity", "2", "Question B", "text", "Help B");
        QuestionEntity q3 = buildQuestion(questionnaireId, "Technical", "Architecture", "3", "Question C", "text", "Help C");

        questionRepository.saveAll(List.of(q1, q2, q3));
        questionRepository.flush();

        List<String> tabs = questionRepository.findDistinctQuestionTabsByQuestionnaireId(questionnaireId);

        assertEquals(List.of("Standard", "Technical"), tabs);
    }

    @Test
    void findDistinctQuestionTabsByQuestionnaireId_should_return_empty_list_when_no_question_exists() {
        List<String> tabs = questionRepository.findDistinctQuestionTabsByQuestionnaireId(UUID.randomUUID());

        assertTrue(tabs.isEmpty());
    }

    private QuestionEntity buildQuestion(
            UUID questionnaireId,
            String tab,
            String category,
            String index,
            String text,
            String type,
            String help
    ) {
        QuestionEntity q = new QuestionEntity();
        q.setQuestionnaireId(questionnaireId);
        q.setTab(tab);
        q.setCategory(category);
        q.setIndex(index);
        q.setText(text);
        q.setType(type);
        q.setHelp(help);
        return q;
    }
}
