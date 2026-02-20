package com.xietg.kc.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ExcelParser {

    private final DataFormatter formatter = new DataFormatter(Locale.ROOT);

    public List<ResponseItem> parseResponsesXlsx(Path path) {
        try (InputStream in = Files.newInputStream(path);
             Workbook wb = new XSSFWorkbook(in)) {

            Sheet ws = wb.getSheet("RESPONSES");
            if (ws == null) {
                throw new ExcelParseException("Missing sheet: RESPONSES");
            }

            if (ws.getPhysicalNumberOfRows() == 0) {
                throw new ExcelParseException("RESPONSES sheet is empty");
            }

            Row headerRow = ws.getRow(0);
            if (headerRow == null) {
                throw new ExcelParseException("RESPONSES sheet is empty");
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell c = headerRow.getCell(i);
                String v = c == null ? "" : formatter.formatCellValue(c);
                headers.add(v == null ? "" : v.trim().toLowerCase(Locale.ROOT));
            }

            int qIdx = headers.indexOf("question_id");
            int aIdx = headers.indexOf("answer");
            if (qIdx < 0 || aIdx < 0) {
                throw new ExcelParseException("RESPONSES must have columns: question_id, answer");
            }

            List<ResponseItem> out = new ArrayList<>();

            int lastRow = ws.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = ws.getRow(r);
                if (row == null) continue;

                String q = cellString(row.getCell(qIdx));
                if (q == null || q.isBlank()) {
                    continue;
                }

                String a = cellString(row.getCell(aIdx));
                if (a != null) a = a.trim();
                out.add(new ResponseItem(q.trim(), (a == null || a.isBlank()) ? null : a));
            }

            if (out.isEmpty()) {
                throw new ExcelParseException("No responses found in RESPONSES");
            }

            return out;

        } catch (ExcelParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelParseException("Failed to read workbook");
        }
    }

    private String cellString(Cell cell) {
        if (cell == null) return null;
        String v = formatter.formatCellValue(cell);
        return v == null ? null : v.trim();
    }

    public record ResponseItem(String questionId, String rawAnswer) {}
}
