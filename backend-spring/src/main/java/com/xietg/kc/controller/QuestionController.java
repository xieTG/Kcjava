package com.xietg.kc.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Map;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class QuestionController {
	
	@GetMapping("/question")
    public Map<String, Object> question() {
        return Map.of("question", true);
    }

}
