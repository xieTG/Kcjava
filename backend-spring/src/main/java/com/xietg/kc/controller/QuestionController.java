package com.xietg.kc.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class QuestionController {
	
	@GetMapping("/question")
    public Map<String, Object> question() {
        return Map.of("question", true);
    }

}
