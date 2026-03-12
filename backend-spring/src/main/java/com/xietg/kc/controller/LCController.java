package com.xietg.kc.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.QuestionEntity;
import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.error.BusinessException;
import com.xietg.kc.excel.ExcelBuilder;
import com.xietg.kc.security.CurrentUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


@SecurityRequirement(name = "bearerAuth")
@RestController
public class LCController {

	private final LCRepository lcRepository;
	private final CurrentUserService currentUserService;
	private final ExcelBuilder templateBuilder;
	private final QuestionRepository questionRepository;
	private final QuestionnaireRepository questionnaireRepository;

	public LCController(
			LCRepository lcRepository,
			CurrentUserService currentUserService,
			QuestionRepository questionRepository,
			ExcelBuilder templateBuilder,
			QuestionnaireRepository questionnaireRepository)
	{
		this.lcRepository = lcRepository;
		this.currentUserService = currentUserService;
		this.templateBuilder=templateBuilder;
		this.questionRepository=questionRepository;
		this.questionnaireRepository=questionnaireRepository;
	}

	@GetMapping("/lcs")
	public List<LCDto> listLC() 
	{
		currentUserService.requireCurrentUser();
		return lcRepository.findAll()
				.stream()
				.map(lc -> new LCDto(lc.getId(), lc.getName(),lc.getYear(), lc.getDescription(),lc.getQuestionnaireId()))
				.toList();

	}

	@PostMapping("/lcs")
	public LCDto createLC(@Valid @RequestBody LCRequest req) 
	{
		currentUserService.requireCurrentUser();

		if(lcRepository.findByName(req.name).isEmpty() != true){
			throw new BusinessException(HttpStatus.OK, "LC Already Exists");
		}

		LCEntity LC = new LCEntity();
		LC.setName(req.name);
		LC.setDescription(req.description);
		LC.setYear(req.year);
		LC.setQuestionnaireId(req.questionnaire);
		lcRepository.save(LC);


		return new LCDto(LC.getId(),LC.getName(), LC.getYear(),LC.getDescription(),LC.getQuestionnaireId());
	}

	@GetMapping("/lcs/{lcId}/xlsx")
	public ResponseEntity<byte[]> downloadXLSX(@PathVariable UUID lcId) {
		// We should be able to download the questionnaire for an LC. If no questionnaire -> return error

		currentUserService.requireCurrentUser();

		if(lcId==null ||lcId.toString().isEmpty())
		{
			throw new BusinessException(HttpStatus.BAD_REQUEST,"Missing LC Id");
		}

		//1- Get the LC
		LCEntity lc = lcRepository.findById(lcId).orElseThrow(() -> BusinessException.notFound("LC not found"));
		if(lc.getQuestionnaireId()==null ||lc.getQuestionnaireId().toString().isEmpty())
		{
			throw new BusinessException(HttpStatus.BAD_REQUEST,"LC does have any questionnaire ID");
		}

		//2- Get the questionnaire associated to the LC
		QuestionnaireEntity questionnaire = questionnaireRepository.findById(lc.getQuestionnaireId()).orElseThrow(() -> BusinessException.notFound("No questionnaire found"));

		//3- Get the questionnaire questions tab
		List<String> questionTabsList = questionRepository.findDistinctQuestionTabsByQuestionnaireId(lc.getQuestionnaireId());



		//4- Get the questionnaire question list
		List<QuestionEntity> questionList = questionRepository.findAllByQuestionnaireId(questionnaire.getId());

		//4- Build the questionnaire template file    	
		byte[] bytes = templateBuilder.buildQuestionsXlsx(questionTabsList,questionList) ;
		String fn = safeFilename(lc.getName()) + "_v" + lc.getYear() + "_template.xlsx";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fn + "\"")
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(bytes);
	}

	@PutMapping("/lcs/{lcId}/questionnaire")
	public LCDto attachQuestionnaireToLC(@PathVariable UUID lcId,@RequestBody AttachQuestionnaireRequest req) 
	{
		currentUserService.requireCurrentUser();


		//1- Check if the LC existe
		LCEntity LC = lcRepository.findById(lcId).orElseThrow(() -> BusinessException.notFound("LC not found"));
		
		//2- Set the LC's questionnaire
		LC.setQuestionnaireId(req.questionnaireId);
		
		//3- Save the LC
		lcRepository.save(LC);

		return new LCDto(LC.getId(),LC.getName(), LC.getYear(),LC.getDescription(),LC.getQuestionnaireId());
	}



	private String safeFilename(String s) {
		if (s == null) return "questionnaire";
		String out = s.trim().toLowerCase(Locale.ROOT);
		out = out.replaceAll("[^a-z0-9._-]+", "_");
		if (out.length() > 80) out = out.substring(0, 80);
		if (out.isBlank()) return "questionnaire";
		return out;
	}

	public record LCDto(UUID id, String name, Integer year, String description, UUID questionnaire) {}
	public record LCRequest(@NotBlank String name, Integer year, String description, UUID questionnaire ) {}
	public record AttachQuestionnaireRequest(UUID questionnaireId) {}


}
