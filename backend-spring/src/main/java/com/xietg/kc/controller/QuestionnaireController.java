package com.xietg.kc.controller;

import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.error.BusinessException;
import com.xietg.kc.security.CurrentUserService;
import com.xietg.kc.service.SubmissionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class QuestionnaireController
{

	private final QuestionnaireRepository questionnaireRepository;

	private final LCRepository lcRepository;

	private final CurrentUserService currentUserService;
	private final SubmissionService submissionService;

	public QuestionnaireController(QuestionnaireRepository questionnaireRepository, LCRepository lcRepository,
			CurrentUserService currentUserService, SubmissionService submissionService)
	{
		this.questionnaireRepository = questionnaireRepository;
		this.lcRepository = lcRepository;
		this.currentUserService = currentUserService;
		this.submissionService = submissionService;
	}

	@GetMapping("/questionnaires")
	public List<QuestionnaireDto> listQuestionnaires()
	{
		currentUserService.requireCurrentUser();
		
		return questionnaireRepository.findByStatus("published").stream()
				.map(q -> new QuestionnaireDto(q.getId().toString(), q.getName(), q.getVersion())).toList();
	}

	@PostMapping(value = "/lcs/{lcId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public SubmissionResponse uploadSubmission(@PathVariable UUID lcId, @RequestPart("file") MultipartFile file)
	{
		UserEntity user = currentUserService.requireCurrentUser();

		// 1- Check if the LC as already a questionnnaire
		LCEntity lc = lcRepository.findById(lcId).orElseThrow(() -> BusinessException.notFound("LC not found"));

		// Set or update questionnaire
		SubmissionService.SubmissionResult result = submissionService.createSubmission(lc, user, file);
		return new SubmissionResponse(result.submissionId().toString(), result.status());
	}

	public record QuestionnaireDto(String id, String name, Integer version)
	{
	}

	public record SubmissionResponse(String submission_id, String status)
	{
	}
}
