package com.xietg.kc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.xietg.kc.controller.AuthController.LoginRequest;
import com.xietg.kc.controller.AuthController.LoginResponse;
import com.xietg.kc.controller.QuestionnaireController.QuestionnaireDto;
import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.error.ApiException;
import com.xietg.kc.excel.TemplateBuilder;
import com.xietg.kc.log.Log;
import com.xietg.kc.security.AuthService;
import com.xietg.kc.service.SubmissionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class LCController {

	private final LCRepository lcRepository;
    private final AuthService authService;

    public LCController(
            LCRepository lcRepository,
            AuthService authService
    ) {
        this.lcRepository = lcRepository;
        this.authService = authService;
    }

    @GetMapping("/LC")
    public List<LCDto> listLC() 
    {
    	
    	return lcRepository.findAll()
                .stream()
                .map(q -> new LCDto(q.getId().toString(), q.getName(), q.getDescription()))
                .toList();
    	
    	
    	/*
    	
    	Log.debug("/api/LC");
    	List<LCDto> list = new ArrayList<LCDto>() ;
    	
    	list.add(new LCDto("A","AA",0));
    	list.add(new LCDto("B","BB",0));
    	list.add(new LCDto("C","CC",0));
    	list.add(new LCDto("D","DD",0));
    	
    	return list;
    	
    	*/
    	
    }
    
    @PostMapping("/LC")
    public LCCreateResponse createLC(@Valid @RequestBody LCRequest req) {
        String name = req.name;
        String description = req.description;
        
        boolean test = lcRepository.findByName(name).isEmpty();
        
        if(test != true){
            throw new ApiException(HttpStatus.OK, "LC Already Exists");
        }
        
        LCEntity LC = new LCEntity();
        LC.setName(name);
        LC.setDescription(description);
        
        lcRepository.save(LC);
        

        return new LCCreateResponse(LC.getId().toString());
    }
	
    public record LCDto(String id, String name, String description) {}
    public record LCRequest(@NotBlank String name,String description ) {}

    public record LCCreateResponse(String id) {}
}
