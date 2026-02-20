package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.QuestionnaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionnaireRepository extends JpaRepository<QuestionnaireEntity, UUID> {
    List<QuestionnaireEntity> findByStatus(String status);
}
