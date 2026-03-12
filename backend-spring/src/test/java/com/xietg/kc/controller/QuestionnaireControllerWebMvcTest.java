package com.xietg.kc.controller;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.security.CurrentUserService;
import com.xietg.kc.security.AuthService;
import org.springframework.http.HttpHeaders;
import com.xietg.kc.service.SubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = QuestionnaireController.class, properties = "app.cors-origins=http://localhost:3000")
@EnableConfigurationProperties(AppProperties.class)
class QuestionnaireControllerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    QuestionnaireRepository questionnaireRepository;

    @MockitoBean
    LCRepository lcRepository;

    @MockitoBean
    CurrentUserService currentUserService;
    
    @MockitoBean
    AuthService authService;

    @MockitoBean
    SubmissionService submissionService;

    private UserEntity authenticatedUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@example.com");
        return user;
    }

    @Test
    void listQuestionnaires_should_return_published_questionnaires() throws Exception {
        UUID q1Id = UUID.randomUUID();
        UUID q2Id = UUID.randomUUID();

        QuestionnaireEntity q1 = new QuestionnaireEntity();
        q1.setId(q1Id);
        q1.setName("Leadership Compass 2026");
        q1.setVersion(1);

        QuestionnaireEntity q2 = new QuestionnaireEntity();
        q2.setId(q2Id);
        q2.setName("IAM Assessment 2026");
        q2.setVersion(3);

        when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
        when(currentUserService.requireCurrentUser()).thenReturn(authenticatedUser());
        when(questionnaireRepository.findByStatus("published"))
                .thenReturn(List.of(q1, q2));

        mvc.perform(get("/questionnaires")
           .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(q1Id.toString()))
           .andExpect(jsonPath("$[0].name").value("Leadership Compass 2026"))
           .andExpect(jsonPath("$[0].version").value(1))
           .andExpect(jsonPath("$[1].id").value(q2Id.toString()))
           .andExpect(jsonPath("$[1].name").value("IAM Assessment 2026"))
           .andExpect(jsonPath("$[1].version").value(3));
    }

    @Test
    void uploadSubmission_should_return_submission_id_and_status() throws Exception {
        UUID lcId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        UserEntity user = authenticatedUser();

        LCEntity lc = new LCEntity();
        lc.setId(lcId);
        lc.setName("Microsoft");
        lc.setDescription("Microsoft LC");
        lc.setYear(2026);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "answers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-xlsx-content".getBytes()
        );

        when(authService.requireUser(anyString())).thenReturn(authenticatedUser());
        when(currentUserService.requireCurrentUser()).thenReturn(user);
        when(lcRepository.findById(lcId)).thenReturn(Optional.of(lc));
        when(submissionService.createSubmission(any(LCEntity.class), any(UserEntity.class), any()))
                .thenReturn(new SubmissionService.SubmissionResult(submissionId, "parsed_ok"));

        mvc.perform(multipart("/lcs/{lcId}/submissions", lcId)
                .file(file)
                .header("Authorization", "Bearer token"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.submission_id").value(submissionId.toString()))
           .andExpect(jsonPath("$.status").value("parsed_ok"));
    }
}