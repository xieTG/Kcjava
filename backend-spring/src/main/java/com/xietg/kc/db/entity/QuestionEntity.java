package com.xietg.kc.db.entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "questions")

/**
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  questionnaire_id UUID NOT NULL REFERENCES questionnaires(id),
  question_category TEXT NOT NULL,
  question_index TEXT NOT NULL,
  question_text TEXT NOT NULL,
  question_type TEXT NOT NULL, // Text||Numeric||percentage||boolean||comment||choices
  question_help TEXT 

*/
public class QuestionEntity {
	
	@Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private UUID questionnaire_id;

    @Column(nullable = false)
    private String question_category;

    @Column(nullable = false)
    private String question_index;

    @Column(nullable = false)
    private String question_text;

    @Column(nullable = false)
    private String question_type;
    
    @Column(nullable = true)
    private String question_help;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    
    public void setQuestionnaireId(UUID id) {
    	this.questionnaire_id =id;
    }

    public String getCategory() {
        return question_category;
    }

    public void setCategory(String category) {
        this.question_category = category;
    }

    public String getIndex() {
        return question_index;
    }

    public void setIndex(String index) {
        this.question_index = index;
    }

    public String getText() {
        return question_text;
    }

    public void setText(String text) {
        this.question_text = text;
    }

    public String getType() {
        return question_type;
    }

    public void setType(String type) {
        this.question_type = type;
    }

    public String getHelp() {
        return question_help;
    }

    public void setHelp(String help) {
        this.question_help = help;
    }

}
