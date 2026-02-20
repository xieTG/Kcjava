package com.xietg.kc.db.repo;

import com.xietg.kc.db.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {
    List<SubmissionEntity> findByUserIdOrderBySubmittedAtDesc(UUID userId);
}
