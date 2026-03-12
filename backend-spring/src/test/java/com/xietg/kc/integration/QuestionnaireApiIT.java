package com.xietg.kc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.repo.QuestionnaireRepository;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionnaireApiIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionnaireRepository repository;

    
    @Test
    void shouldReturnAllQuestionnaires() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	 String authBody = """
    	            {
    	              "email": "it-lc-questionnaire-flow@test.com",
    	              "password": "Password123!"
    	            }
    	        """;

    	        mockMvc.perform(post("/auth/register")
    	                        .contentType(MediaType.APPLICATION_JSON)
    	                        .content(authBody))
    	                .andExpect(status().isOk());

    	        String loginResponse = mockMvc.perform(post("/auth/login")
    	                        .contentType(MediaType.APPLICATION_JSON)
    	                        .content(authBody))
    	                .andExpect(status().isOk())
    	                .andReturn()
    	                .getResponse()
    	                .getContentAsString();
    	
    	        @SuppressWarnings("unchecked")
    	        Map<String, String> json = objectMapper.readValue(loginResponse, Map.class);

    	        String token = json.get("access_token");
    	
        QuestionnaireEntity q = new QuestionnaireEntity();
        q.setId(UUID.randomUUID());
        q.setName("IT Questionnaire");
        q.setVersion(1);
        q.setStatus("published");
        repository.save(q);

        String body = mockMvc.perform(get("/questionnaires")
        		.header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("IT Questionnaire");
    }
}