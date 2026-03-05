package com.xietg.kc.controller;

import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.entity.SubmissionStatus;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.error.ApiException;
import com.xietg.kc.excel.TemplateBuilder;
import com.xietg.kc.security.AuthService;
import com.xietg.kc.service.SubmissionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final LCRepository lcRepository;
    private final TemplateBuilder templateBuilder;
    private final AuthService authService;
    private final SubmissionService submissionService;

    public QuestionnaireController(
            QuestionnaireRepository questionnaireRepository,
            LCRepository lcRepository,
            TemplateBuilder templateBuilder,
            AuthService authService,
            SubmissionService submissionService
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.lcRepository = lcRepository;
        this.templateBuilder = templateBuilder;
        this.authService = authService;
        this.submissionService = submissionService;
    }

    @GetMapping("/questionnaires")
    public List<QuestionnaireDto> listQuestionnaires() 
    {
    	return questionnaireRepository.findByStatus("published")
                .stream()
                .map(q -> new QuestionnaireDto(q.getId().toString(), q.getName(), q.getVersion()))
                .toList();
    }

    @GetMapping("/questionnaires/{LCId}/template")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable UUID questionnaireId) {
    	//TODO: Change questionnaireId into lcId. We should be able to download the questionnaire for an LC. If no questionnaire -> return error
    	
        QuestionnaireEntity q = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> ApiException.notFound("Questionnaire not found"));

        byte[] bytes = templateBuilder.buildTemplate();
        String fn = safeFilename(q.getName()) + "_v" + q.getVersion() + "_template.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fn + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/questionnaires/{lcId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SubmissionResponse uploadSubmission(
            @PathVariable UUID lcId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        UserEntity user = authService.requireUser(authorization);
        
        //1- Check if the LC as already a questionnnaire
        LCEntity lc = lcRepository.findById(lcId).orElseThrow(() -> ApiException.notFound("LC not found"));
        
        
        //Set or update questionnaire
        SubmissionService.SubmissionResult result = submissionService.createSubmission(lc, user, file);
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
