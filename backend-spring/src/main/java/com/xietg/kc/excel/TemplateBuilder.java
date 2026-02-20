package com.xietg.kc.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class TemplateBuilder {

    public byte[] buildTemplate() {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet questions = wb.createSheet("QUESTIONS");
            writeRow(questions, 0, List.of("question_id", "section", "question_text", "type", "choices", "required"));

            // MVP: hardcoded questions (matches Python backend)
            writeRow(questions, 1, List.of("SEC_01", "Security", "MFA est-il activé ?", "boolean", "", "true"));
            writeRow(questions, 2, List.of("SEC_02", "Security", "Chiffrement au repos ?", "boolean", "", "true"));
            writeRow(questions, 3, List.of("OPS_01", "Operations", "CI/CD en place ?", "boolean", "", "false"));
            writeRow(questions, 4, List.of("OPS_02", "Operations", "Fréquence des releases ?", "single_choice", "daily|weekly|monthly|quarterly|other", "false"));

            Sheet responses = wb.createSheet("RESPONSES");
            writeRow(responses, 0, List.of("question_id", "answer"));

            // Pre-fill question ids in RESPONSES
            for (int r = 1; r <= 4; r++) {
                String qid = cellString(questions, r, 0);
                if (qid != null && !qid.isBlank()) {
                    writeRow(responses, responses.getLastRowNum() + 1, List.of(qid, ""));
                }
            }

            // Autosize just for a nicer file; optional
            for (int i = 0; i < 6; i++) {
                questions.autoSizeColumn(i);
            }
            responses.autoSizeColumn(0);
            responses.autoSizeColumn(1);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    private void writeRow(Sheet sheet, int rowIdx, List<String> values) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < values.size(); i++) {
            row.createCell(i).setCellValue(values.get(i));
        }
    }

    private String cellString(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return null;
        var cell = row.getCell(colIdx);
        if (cell == null) return null;
        return cell.getStringCellValue();
    }
}
