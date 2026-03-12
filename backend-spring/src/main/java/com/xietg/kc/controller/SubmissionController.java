package com.xietg.kc.controller;

import com.xietg.kc.db.entity.SubmissionEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.SubmissionRepository;
import com.xietg.kc.security.AuthService;
import com.xietg.kc.security.CurrentUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class SubmissionController {


	private final CurrentUserService currentUserService;
    private final SubmissionRepository submissionRepository;

    public SubmissionController(CurrentUserService currentUserService, SubmissionRepository submissionRepository) {
        this.currentUserService = currentUserService;
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/submissions")
    public List<MySubmissionDto> mySubmissions(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        UserEntity user = currentUserService.requireCurrentUser();

        List<SubmissionEntity> subs = submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId());

        return subs.stream()
                .map(s -> new MySubmissionDto(
                        s.getId().toString(),
                        s.getLCId().toString(),
                        s.getStatus().name(),
                        s.getSubmittedAt(),
                        s.getErrorJson()
                ))
                .toList();
    }

    public record MySubmissionDto(
            String id,
            String lc_id,
            String status,
            OffsetDateTime submitted_at,
            Map<String, Object> error
    ) {}
}
