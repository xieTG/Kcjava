package com.xietg.kc.controller;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.QuestionEntity;
import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.excel.ExcelBuilder;
import com.xietg.kc.security.CurrentUserService;
import com.xietg.kc.security.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LCController.class, properties = "app.cors-origins=http://localhost:3000")
@EnableConfigurationProperties(AppProperties.class)
class LCControllerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    LCRepository lcRepository;

    @MockitoBean 
    CurrentUserService currentUserService;
    
    @MockitoBean 
    AuthService authService;

    @MockitoBean
    ExcelBuilder templateBuilder;

    @MockitoBean
    QuestionRepository questionRepository;

    @MockitoBean
    QuestionnaireRepository questionnaireRepository;

    private UserEntity authenticatedUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@example.com");
        return user;
    }

    @Test
    void listLC_should_return_all_lcs() throws Exception {
    	when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
    	when(currentUserService.requireCurrentUser()).thenReturn(authenticatedUser());

        UUID lcId = UUID.randomUUID();
        UUID questionnaireId = UUID.randomUUID();

        LCEntity lc = new LCEntity();
        lc.setId(lcId);
        lc.setName("Microsoft");
        lc.setYear(2026);
        lc.setDescription("Microsoft LC");
        lc.setQuestionnaireId(questionnaireId);

        when(lcRepository.findAll()).thenReturn(List.of(lc));

        mvc.perform(get("/lcs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(lcId.toString()))
           .andExpect(jsonPath("$[0].name").value("Microsoft"))
           .andExpect(jsonPath("$[0].year").value(2026))
           .andExpect(jsonPath("$[0].description").value("Microsoft LC"))
           .andExpect(jsonPath("$[0].questionnaire").value(questionnaireId.toString()));
    }

    @Test
    void createLC_should_create_and_return_lc() throws Exception {
    	when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
    	when(currentUserService.requireCurrentUser()).thenReturn(authenticatedUser());
        when(lcRepository.findByName("Microsoft")).thenReturn(List.of());
        when(lcRepository.save(any(LCEntity.class))).thenAnswer(invocation -> {
            LCEntity lc = invocation.getArgument(0);
            if (lc.getId() == null) {
                lc.setId(UUID.randomUUID());
            }
            return lc;
        });

        String body = """
            {
              "name": "Microsoft",
              "year": 2026,
              "description": "Microsoft LC",
              "questionnaire": null
            }
            """;

        mvc.perform(post("/lcs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").isNotEmpty())
           .andExpect(jsonPath("$.name").value("Microsoft"))
           .andExpect(jsonPath("$.year").value(2026))
           .andExpect(jsonPath("$.description").value("Microsoft LC"))
           .andExpect(jsonPath("$.questionnaire").doesNotExist());
    }

    @Test
    void createLC_should_return_bad_request_when_name_is_blank() throws Exception {
    	
    	when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
    	when(currentUserService.requireCurrentUser()).thenReturn(authenticatedUser());

        String body = """
            {
              "name": "",
              "year": 2026,
              "description": "Invalid LC",
              "questionnaire": null
            }
            """;

        mvc.perform(post("/lcs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void attachQuestionnaireToLC_should_update_and_return_lc() throws Exception {
    	when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
    	when(currentUserService.requireCurrentUser()).thenReturn(authenticatedUser());

        UUID lcId = UUID.randomUUID();
        UUID questionnaireId = UUID.randomUUID();

        LCEntity lc = new LCEntity();
        lc.setId(lcId);
        lc.setName("Microsoft");
        lc.setYear(2026);
        lc.setDescription("Microsoft LC");

        when(lcRepository.findById(lcId)).thenReturn(Optional.of(lc));
        when(lcRepository.save(any(LCEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = """
            {
              "questionnaireId": "%s"
            }
            """.formatted(questionnaireId);

        mvc.perform(put("/lcs/{lcId}/questionnaire", lcId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(lcId.toString()))
           .andExpect(jsonPath("$.name").value("Microsoft"))
           .andExpect(jsonPath("$.questionnaire").value(questionnaireId.toString()));
    }

    @Test
    void downloadXlsx_should_return_file() throws Exception {
    	when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
    	when(currentUserService.requireCurrentUser()).thenReturn(authenticatedUser());

        UUID lcId = UUID.randomUUID();
        UUID questionnaireId = UUID.randomUUID();

        LCEntity lc = new LCEntity();
        lc.setId(lcId);
        lc.setName("Microsoft");
        lc.setYear(2026);
        lc.setDescription("Microsoft LC");
        lc.setQuestionnaireId(questionnaireId);

        QuestionnaireEntity questionnaire = new QuestionnaireEntity();
        questionnaire.setId(questionnaireId);
        questionnaire.setName("Questionnaire 2026");

        QuestionEntity question = new QuestionEntity();
        question.setQuestionnaireId(questionnaireId);
        question.setTab("Standard");
        question.setCategory("Identity");
        question.setIndex("1");
        question.setText("What is your IAM model?");
        question.setType("text");
        question.setHelp("Describe the model");

        byte[] xlsxBytes = "fake-xlsx-content".getBytes();

        when(lcRepository.findById(lcId)).thenReturn(Optional.of(lc));
        when(questionnaireRepository.findById(questionnaireId)).thenReturn(Optional.of(questionnaire));
        when(questionRepository.findDistinctQuestionTabsByQuestionnaireId(questionnaireId))
                .thenReturn(List.of("Standard"));
        when(questionRepository.findAllByQuestionnaireId(questionnaireId))
                .thenReturn(List.of(question));
        when(templateBuilder.buildQuestionsXlsx(anyList(), anyList()))
                .thenReturn(xlsxBytes);

        mvc.perform(get("/lcs/{lcId}/xlsx", lcId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
           .andExpect(status().isOk())
           .andExpect(header().string(
                   HttpHeaders.CONTENT_DISPOSITION,
                   "attachment; filename=\"microsoft_v2026_template.xlsx\""))
           .andExpect(content().contentType(
                   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
           .andExpect(content().bytes(xlsxBytes));
    }
}