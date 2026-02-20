package com.xietg.kc.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "submissions")
public class SubmissionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "questionnaire_id", nullable = false, columnDefinition = "uuid")
    private UUID questionnaireId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "uploaded_file_key")
    private String uploadedFileKey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "submission_status")
    private SubmissionStatus status = SubmissionStatus.received;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "parsed_at")
    private OffsetDateTime parsedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_json", columnDefinition = "jsonb")
    private Map<String, Object> errorJson;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = SubmissionStatus.received;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(UUID questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUploadedFileKey() {
        return uploadedFileKey;
    }

    public void setUploadedFileKey(String uploadedFileKey) {
        this.uploadedFileKey = uploadedFileKey;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public OffsetDateTime getParsedAt() {
        return parsedAt;
    }

    public void setParsedAt(OffsetDateTime parsedAt) {
        this.parsedAt = parsedAt;
    }

    public Map<String, Object> getErrorJson() {
        return errorJson;
    }

    public void setErrorJson(Map<String, Object> errorJson) {
        this.errorJson = errorJson;
    }
}
