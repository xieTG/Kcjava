package com.xietg.kc.service;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.*;
import com.xietg.kc.db.repo.AnswerRepository;
import com.xietg.kc.db.repo.QuestionRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.db.repo.SubmissionRepository;
import com.xietg.kc.error.ApiException;
import com.xietg.kc.excel.ExcelParseException;
import com.xietg.kc.excel.ExcelParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class SubmissionService {

	private static final long MAX_XLSX_BYTES = 15L * 1024L * 1024L;

	private final AppProperties props;
	private final QuestionnaireRepository questionnaireRepository;
	private final SubmissionRepository submissionRepository;
	private final QuestionRepository questionRepository;
	private final ExcelParser excelParser;

	public SubmissionService(	AppProperties props,
			QuestionnaireRepository questionnaireRepository,
			SubmissionRepository submissionRepository,
			QuestionRepository questionRepository,
			ExcelParser excelParser) {
		this.props = props;
		this.questionnaireRepository = questionnaireRepository;
		this.submissionRepository = submissionRepository;
		this.questionRepository = questionRepository;
		this.excelParser = excelParser;
	}

	@Transactional(noRollbackFor = ApiException.class)
	public SubmissionResult createSubmission(UUID questionnaireId, UserEntity user, MultipartFile file) {

		QuestionnaireEntity q = questionnaireRepository.findById(questionnaireId)
				.orElseThrow(() -> ApiException.notFound("Questionnaire not found"));

		if (file == null || file.isEmpty()) {
			throw ApiException.badRequest("Missing file");
		}

		String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
		if (!filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
			throw ApiException.badRequest("Only .xlsx files are supported");
		}

		if (file.getSize() > MAX_XLSX_BYTES) {
			throw ApiException.badRequest("File too large (max 15MB)");
		}

		UUID submissionId = UUID.randomUUID();
		String storedKey = submissionId + ".xlsx";
		Path dest = props.getUploadDir().resolve(storedKey);

		// Create submission record first (status=received)
		SubmissionEntity submission = new SubmissionEntity();
		submission.setId(submissionId);
		submission.setQuestionnaireId(q.getId());
		submission.setUserId(user.getId());
		submission.setUploadedFileKey(storedKey);
		submission.setStatus(SubmissionStatus.received);
		submissionRepository.save(submission);

		try {
			Files.createDirectories(props.getUploadDir());
			file.transferTo(dest);

			// Parse Standard and Tech question tab to get the full question list
			List<QuestionEntity> questionsList = excelParser.parseQuestionsXlsx(questionnaireId,dest,"4. Standard Questions","5. Technical Questions");
			questionRepository.saveAll(questionsList);

			//Confirm parsing is OK
			submission.setStatus(SubmissionStatus.parsed_ok);
			submission.setParsedAt(OffsetDateTime.now(ZoneOffset.UTC));
			submission.setErrorJson(null);
			submissionRepository.save(submission);

			return new SubmissionResult(submissionId, submission.getStatus().name());

		} catch (ExcelParseException e) {
			submission.setStatus(SubmissionStatus.parse_error);
			submission.setParsedAt(OffsetDateTime.now(ZoneOffset.UTC));
			submission.setErrorJson(Map.of("error", e.getMessage()));
			submissionRepository.save(submission);

			throw new ApiException(HttpStatus.BAD_REQUEST, "Excel parse error: " + e.getMessage());

		} catch (ApiException e) {
			// Make sure we don't leave a dangling submission for request validation errors.
			// Here, ApiException could still happen; we keep behavior explicit.
			throw e;

		} catch (Exception e) {
			// On unexpected errors, keep the submission but mark it as parse_error for visibility
			submission.setStatus(SubmissionStatus.parse_error);
			submission.setParsedAt(OffsetDateTime.now(ZoneOffset.UTC));
			submission.setErrorJson(Map.of("error", "Internal error"));
			submissionRepository.save(submission);

			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
		}
	}

	public record SubmissionResult(UUID submissionId, String status) {}
}
