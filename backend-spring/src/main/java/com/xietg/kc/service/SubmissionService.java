package com.xietg.kc.service;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.*;
import com.xietg.kc.db.repo.*;
import com.xietg.kc.error.BusinessException;
import com.xietg.kc.excel.*;
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
	private final LCRepository lcRepository;
	private final SubmissionRepository submissionRepository;
	private final QuestionRepository questionRepository;
	private final ExcelParser excelParser;

	public SubmissionService(	AppProperties props,
			QuestionnaireRepository questionnaireRepository,
			LCRepository lcRepository,
			SubmissionRepository submissionRepository,
			QuestionRepository questionRepository,
			ExcelParser excelParser) {
		this.props = props;
		this.questionnaireRepository = questionnaireRepository;
		this.lcRepository = lcRepository;
		this.submissionRepository = submissionRepository;
		this.questionRepository = questionRepository;
		this.excelParser = excelParser;
	}

	@Transactional(noRollbackFor = BusinessException.class)
	public SubmissionResult createSubmission( LCEntity lc , UserEntity user, MultipartFile file) {

		    	
		QuestionnaireEntity questionnaire = new QuestionnaireEntity();
					
		if(lc.getQuestionnaireId()==null)
		{
			//We must create the questionnaire in the DB
			UUID id = UUID.randomUUID();
			questionnaire = new QuestionnaireEntity();
			questionnaire.setId(id);
			questionnaire.setName("Name for"+id.toString());
			questionnaire.setVersion(0);
			questionnaireRepository.save(questionnaire);
			
			lc.setQuestionnaireId(id);
			lcRepository.save(lc);
			
		}
		else
		{
			Optional<QuestionnaireEntity> q = questionnaireRepository.findById(lc.getQuestionnaireId());
			questionnaire=q.get();
		}
		

		if (file == null || file.isEmpty()) {
			throw BusinessException.badRequest("Missing file");
		}

		String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
		if (!filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
			throw BusinessException.badRequest("Only .xlsx files are supported");
		}

		if (file.getSize() > MAX_XLSX_BYTES) {
			throw BusinessException.badRequest("File too large (max 15MB)");
		}

		UUID submissionId = UUID.randomUUID();
		String storedKey = submissionId + ".xlsx";
		Path dest = props.getUploadDir().resolve(storedKey);

		// Create submission record first (status=received)
		SubmissionEntity submission = new SubmissionEntity();
		submission.setId(submissionId);
		submission.setLCId(lc.getId());
		submission.setUserId(user.getId());
		submission.setUploadedFileKey(storedKey);
		submission.setStatus(SubmissionStatus.received);
		submissionRepository.save(submission);

		try {
			Files.createDirectories(props.getUploadDir());
			file.transferTo(dest);

			// Parse Standard and Tech question tab to get the full question list
			List<QuestionEntity> questionsList = excelParser.parseQuestionsXlsx(questionnaire.getId(),dest,"4. Standard Questions","5. Technical Questions");
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

			throw new BusinessException(HttpStatus.BAD_REQUEST, "Excel parse error: " + e.getMessage());

		} catch (BusinessException e) {
			// Make sure we don't leave a dangling submission for request validation errors.
			// Here, ApiException could still happen; we keep behavior explicit.
			throw e;

		} catch (Exception e) {
			// On unexpected errors, keep the submission but mark it as parse_error for visibility
			submission.setStatus(SubmissionStatus.parse_error);
			submission.setParsedAt(OffsetDateTime.now(ZoneOffset.UTC));
			submission.setErrorJson(Map.of("error", "Internal error"));
			submissionRepository.save(submission);

			throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
		}
	}

	public record SubmissionResult(UUID submissionId, String status) {}
}
