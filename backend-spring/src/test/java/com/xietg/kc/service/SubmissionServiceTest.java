package com.xietg.kc.service;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.QuestionnaireEntity;
import com.xietg.kc.db.entity.SubmissionEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.db.repo.SubmissionRepository;
import com.xietg.kc.error.BusinessException;
import com.xietg.kc.excel.ExcelParseException;
import com.xietg.kc.excel.ExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SubmissionServiceTest {

    QuestionnaireRepository questionnaireRepository = mock(QuestionnaireRepository.class);
    LCRepository lcRepository = mock(LCRepository.class);
    SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
    QuestionRepository questionRepository = mock(QuestionRepository.class);
    ExcelParser excelParser = mock(ExcelParser.class);

    SubmissionService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setUploadDir(tempDir);

        service = new SubmissionService(
                props,
                questionnaireRepository,
                lcRepository,
                submissionRepository,
                questionRepository,
                excelParser
        );
    }

    @Test
    void should_reject_missing_file() {
        LCEntity lc = new LCEntity();
        lc.setId(UUID.randomUUID());

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.createSubmission(lc, user, null)
        );

        assertEquals("Missing file", ex.getMessage());
        verifyNoInteractions(excelParser);
    }

    @Test
    void should_reject_non_xlsx_file() {
        LCEntity lc = new LCEntity();
        lc.setId(UUID.randomUUID());

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        MockMultipartFile file =
                new MockMultipartFile("file", "bad.csv", "text/csv", "a,b,c".getBytes());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.createSubmission(lc, user, file)
        );

        assertEquals("Only .xlsx files are supported", ex.getMessage());
        verifyNoInteractions(excelParser);
    }

    @Test
    void should_reject_file_too_large() {
        LCEntity lc = new LCEntity();
        lc.setId(UUID.randomUUID());

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        byte[] largeContent = new byte[16 * 1024 * 1024];
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "answers.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        largeContent
                );

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.createSubmission(lc, user, file)
        );

        assertEquals("File too large (max 15MB)", ex.getMessage());
        verifyNoInteractions(excelParser);
    }

    @Test
    void should_create_submission_and_mark_parsed_ok() throws Exception {
        UUID questionnaireId = UUID.randomUUID();

        LCEntity lc = new LCEntity();
        lc.setId(UUID.randomUUID());
        lc.setQuestionnaireId(questionnaireId);

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        QuestionnaireEntity questionnaire = new QuestionnaireEntity();
        questionnaire.setId(questionnaireId);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "answers.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "xlsx-content".getBytes()
                );

        when(questionnaireRepository.findById(questionnaireId))
                .thenReturn(Optional.of(questionnaire));
        when(excelParser.parseQuestionsXlsx(eq(questionnaireId), any(Path.class), eq("4. Standard Questions"), eq("5. Technical Questions")))
                .thenReturn(List.of());

        SubmissionService.SubmissionResult result = service.createSubmission(lc, user, file);

        assertNotNull(result);
        assertEquals("parsed_ok", result.status());

        verify(questionnaireRepository).findById(questionnaireId);
        verify(questionRepository).saveAll(anyList());

        verify(submissionRepository, atLeast(2)).save(any(SubmissionEntity.class));

        assertTrue(Files.list(tempDir).findAny().isPresent());
    }

    @Test
    void should_mark_submission_parse_error_when_excel_parser_fails() {
        UUID questionnaireId = UUID.randomUUID();

        LCEntity lc = new LCEntity();
        lc.setId(UUID.randomUUID());
        lc.setQuestionnaireId(questionnaireId);

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        QuestionnaireEntity questionnaire = new QuestionnaireEntity();
        questionnaire.setId(questionnaireId);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "answers.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "xlsx-content".getBytes()
                );

        when(questionnaireRepository.findById(questionnaireId))
                .thenReturn(Optional.of(questionnaire));
        when(excelParser.parseQuestionsXlsx(eq(questionnaireId), any(Path.class), eq("4. Standard Questions"), eq("5. Technical Questions")))
                .thenThrow(new ExcelParseException("broken workbook"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.createSubmission(lc, user, file)
        );

        assertTrue(ex.getMessage().contains("Excel parse error"));
        verify(submissionRepository, atLeast(2)).save(any(SubmissionEntity.class));
    }

    @Test
    void should_create_questionnaire_when_lc_has_no_questionnaire_id() {
        LCEntity lc = new LCEntity();
        lc.setId(UUID.randomUUID());

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "answers.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "xlsx-content".getBytes()
                );

        when(excelParser.parseQuestionsXlsx(any(UUID.class), any(Path.class), eq("4. Standard Questions"), eq("5. Technical Questions")))
                .thenReturn(List.of());

        SubmissionService.SubmissionResult result = service.createSubmission(lc, user, file);

        assertEquals("parsed_ok", result.status());
        verify(questionnaireRepository).save(any(QuestionnaireEntity.class));
        verify(lcRepository).save(lc);
        assertNotNull(lc.getQuestionnaireId());
    }
}