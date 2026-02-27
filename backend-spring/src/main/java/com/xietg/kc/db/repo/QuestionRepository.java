package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.QuestionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

}

