package com.xietg.kc.controller;

import com.xietg.kc.db.entity.SubmissionEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.SubmissionRepository;
import com.xietg.kc.security.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class MeController {

    private final AuthService authService;
    private final SubmissionRepository submissionRepository;

    public MeController(AuthService authService, SubmissionRepository submissionRepository) {
        this.authService = authService;
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/me/submissions")
    public List<MySubmissionDto> mySubmissions(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        UserEntity user = authService.requireUser(authorization);

        List<SubmissionEntity> subs = submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId());

        return subs.stream()
                .map(s -> new MySubmissionDto(
                        s.getId().toString(),
                        s.getQuestionnaireId().toString(),
                        s.getStatus().name(),
                        s.getSubmittedAt(),
                        s.getErrorJson()
                ))
                .toList();
    }

    public record MySubmissionDto(
            String id,
            String questionnaire_id,
            String status,
            OffsetDateTime submitted_at,
            Map<String, Object> error
    ) {}
}
