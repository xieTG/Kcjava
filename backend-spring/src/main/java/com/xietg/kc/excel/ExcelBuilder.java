package com.xietg.kc.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.xietg.kc.db.entity.QuestionEntity;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

@Component
public class ExcelBuilder {

	public byte[] buildQuestionsXlsx(List<String> questionTabsList, List<QuestionEntity> questionList) {

		try (Workbook wb = new XSSFWorkbook();
				ByteArrayOutputStream out = new ByteArrayOutputStream()) 
		{
			Iterator <String> it=questionTabsList.iterator();

			while(it.hasNext())
			{
				Sheet questionsSheet = wb.createSheet(it.next());

				Iterator <QuestionEntity> itQE = questionList.iterator();

				String lastQuestionCategory="";
				int rowindex=0;
				while(itQE.hasNext())
				{

					QuestionEntity qE =itQE.next();
					
					String currentCategory = qE.getCategory();
										
					if(!currentCategory.equalsIgnoreCase(lastQuestionCategory))
					{
						//we change category so we must write a category row
						writeRow(questionsSheet, rowindex, List.of("", currentCategory));
						lastQuestionCategory=currentCategory;
					}
					else
					{
						//we are on a question row
						String questionIndex = qE.getIndex();
						int idx = questionIndex.lastIndexOf(')');
						if(idx >= 0)
						{
							//don't display the question index for the subquestions
							questionIndex = "";
						}
												
						writeRow(questionsSheet, rowindex, List.of(questionIndex, qE.getText(), qE.getType(), qE.getHelp()));
					}
					rowindex++;
										
				}
				questionsSheet.autoSizeColumn(0);
				questionsSheet.autoSizeColumn(1);
				questionsSheet.autoSizeColumn(2);
				questionsSheet.autoSizeColumn(3);
			}
			
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
}
