package com.xietg.kc.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.xietg.kc.db.entity.AnswerEntity;
import com.xietg.kc.db.entity.QuestionEntity;
import com.xietg.kc.log.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ExcelParser {



    public List<QuestionEntity> parseQuestionsXlsx(UUID questionnaireId,Path path, String tabSTDQuestions, String tabTECHQuestions) {
        
    	List<QuestionEntity> out = new ArrayList<>();
    	try (InputStream in = Files.newInputStream(path);
             Workbook wb = new XSSFWorkbook(in)) {
        	
    		out = parseQuestionsTab( questionnaireId,wb,  tabSTDQuestions, out);
    		out = parseQuestionsTab( questionnaireId,wb,  tabTECHQuestions, out);
        	
        } catch (ExcelParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelParseException("Failed to read workbook");
        }
        	
        	return out;
        	
        }
        
     final List<QuestionEntity> parseQuestionsTab(UUID questionnaireId,Workbook wb, String tabName, List<QuestionEntity> out)
     {

            Sheet ws = wb.getSheet(tabName);
            if (ws == null) {
                throw new ExcelParseException("Missing tab: "+tabName);
            }

            if (ws.getPhysicalNumberOfRows() == 0) {
                throw new ExcelParseException(tabName+ " is empty");
            }

            Row headerRow = ws.getRow(0);
            if (headerRow == null) {
                throw new ExcelParseException(tabName+ " is empty");
            }

            int lastRow = ws.getLastRowNum();
            
            Log.logs("Number of row: " +lastRow);
                     
            
            
            int r=0,i=0;
            String questionCategory=null;
            String questionIndex = null;
            String subquestionIndex=null;
            for (r = 0; r < lastRow; r++) 
            {
            	
                String questionText = null;
                String questionType = null;
                String questionHelp = null;
                
                Row row = ws.getRow(r);
                if (row == null) continue;
                
                String cell1="",cell2="",cell3="",cell4="";
                
                int numberOfCells= row.getPhysicalNumberOfCells();

                if(numberOfCells > 0)
                	cell1 = row.getCell(0).getStringCellValue();
                if(numberOfCells > 1)
                	cell2 = row.getCell(1).getStringCellValue();
                if(numberOfCells > 2)
                	cell3 = row.getCell(2).getStringCellValue();
                if(numberOfCells > 3)
                	cell4 = row.getCell(3).getStringCellValue();
                
                if(cell1 == "" && cell3=="" && cell4=="")
                {
                	/*Category management in the standard tab*/
                	questionCategory=cell2;
                	System.out.println(questionCategory);
                	i=0;
                	questionIndex = null;
                    subquestionIndex=null;
                }
                else if(cell2 == "" && cell3=="" && cell4=="")
                {
                	/*Category management in the technical tab*/
                	questionCategory=cell1;
                	System.out.println(questionCategory);
                	i=0;
                	questionIndex = null;
                    subquestionIndex=null;
                }
                else if(cell1 == "" && cell4=="")
                {
                	
                	questionIndex= subquestionIndex + "abcdefghijklmnopqrstuvwxyz".charAt(i)+")";
                	questionText = cell2;
                    questionType = cell3;
                    questionHelp = cell4;
                    Log.logs(questionCategory+" "+questionIndex+" "+questionText+" "+questionType+" "+questionHelp);
                    i++;
                }
                else
                {
                	questionIndex = cell1;
                	subquestionIndex = questionIndex;
                    questionText = cell2;
                    questionType = cell3;
                    questionHelp = cell4;
                    Log.logs(questionCategory+" "+questionIndex+" "+questionText+" "+questionType+" "+questionHelp);
                    i=0;
                }
                             
                         
                if(questionText!=null)
                {
	                QuestionEntity q = new QuestionEntity();
	                q.setId(UUID.randomUUID());
	                q.setQuestionnaireId(questionnaireId);
	                q.setIndex(questionIndex);
	                q.setText(questionText);
	                q.setType(questionType);
	                q.setHelp(questionHelp);
	                q.setCategory(questionCategory);
	                out.add(q);
                }
                
            }
            return out;

        
    }

   

}
