package com.xietg.kc.controller;

import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.error.ApiException;
import com.xietg.kc.excel.TemplateBuilder;
import com.xietg.kc.security.AuthService;
import com.xietg.kc.service.SubmissionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
public class QuestionnaireController {

    private final QuestionnaireRepository questionnaireRepository;
    private final TemplateBuilder templateBuilder;
    private final AuthService authService;
    private final SubmissionService submissionService;

    public QuestionnaireController(
            QuestionnaireRepository questionnaireRepository,
            TemplateBuilder templateBuilder,
            AuthService authService,
            SubmissionService submissionService
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.templateBuilder = templateBuilder;
        this.authService = authService;
        this.submissionService = submissionService;
    }

    @GetMapping("/questionnaires")
    public List<QuestionnaireDto> listQuestionnaires() {
        return questionnaireRepository.findByStatus("published")
                .stream()
                .map(q -> new QuestionnaireDto(q.getId().toString(), q.getName(), q.getVersion()))
                .toList();
    }

    @GetMapping("/questionnaires/{questionnaireId}/template")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable UUID questionnaireId) {
        QuestionnaireEntity q = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> ApiException.notFound("Questionnaire not found"));

        byte[] bytes = templateBuilder.buildTemplate();
        String fn = safeFilename(q.getName()) + "_v" + q.getVersion() + "_template.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fn + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/questionnaires/{questionnaireId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SubmissionResponse uploadSubmission(
            @PathVariable UUID questionnaireId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        UserEntity user = authService.requireUser(authorization);
        SubmissionService.SubmissionResult result = submissionService.createSubmission(questionnaireId, user, file);
        return new SubmissionResponse(result.submissionId().toString(), result.status());
    }

    private String safeFilename(String s) {
        if (s == null) return "questionnaire";
        String out = s.trim().toLowerCase(Locale.ROOT);
        out = out.replaceAll("[^a-z0-9._-]+", "_");
        if (out.length() > 80) out = out.substring(0, 80);
        if (out.isBlank()) return "questionnaire";
        return out;
    }

    public record QuestionnaireDto(String id, String name, Integer version) {}

    public record SubmissionResponse(String submission_id, String status) {}
}
