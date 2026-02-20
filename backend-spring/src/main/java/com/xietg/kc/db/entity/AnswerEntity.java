package com.xietg.kc.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "answers")
public class AnswerEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "submission_id", nullable = false, columnDefinition = "uuid")
    private UUID submissionId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "raw_answer", columnDefinition = "text")
    private String rawAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "normalized_json", columnDefinition = "jsonb")
    private Map<String, Object> normalizedJson;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getRawAnswer() {
        return rawAnswer;
    }

    public void setRawAnswer(String rawAnswer) {
        this.rawAnswer = rawAnswer;
    }

    public Map<String, Object> getNormalizedJson() {
        return normalizedJson;
    }

    public void setNormalizedJson(Map<String, Object> normalizedJson) {
        this.normalizedJson = normalizedJson;
    }
}
