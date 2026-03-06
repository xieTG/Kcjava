package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.QuestionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

	
	@Query(value="""
			select *
			from questions
			where questionnaire_id = :questionnaireUuid
			""", nativeQuery = true)
	List<QuestionEntity> findAllByQuestionnaireId(UUID questionnaireUuid);

	@Query(value="""
			select distinct question_tab
			from questions
			where questionnaire_id = :questionnaireUuid
			order by question_tab
			""", nativeQuery = true)
	List<String> findDistinctQuestionTabsByQuestionnaireId(UUID questionnaireUuid);

}

