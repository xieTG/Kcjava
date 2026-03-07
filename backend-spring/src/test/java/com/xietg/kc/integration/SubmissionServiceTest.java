package com.xietg.kc.service;

import com.xietg.kc.config.AppProperties;
import com.xietg.kc.db.entity.LCEntity;
import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.repo.LCRepository;
import com.xietg.kc.db.repo.QuestionRepository;
import com.xietg.kc.db.repo.QuestionnaireRepository;
import com.xietg.kc.db.repo.SubmissionRepository;
import com.xietg.kc.error.BusinessException;
import com.xietg.kc.excel.ExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
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
    void should_reject_non_xlsx_file() {
        LCEntity lc = new LCEntity();
        lc.setId(java.util.UUID.randomUUID());

        UserEntity user = new UserEntity();
        user.setId(java.util.UUID.randomUUID());

        MockMultipartFile file =
                new MockMultipartFile("file", "bad.csv", "text/csv", "a,b,c".getBytes());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.createSubmission(lc, user, file)
        );

        assertEquals("Only .xlsx files are supported", ex.getMessage());
        verifyNoInteractions(excelParser);
    }
}
