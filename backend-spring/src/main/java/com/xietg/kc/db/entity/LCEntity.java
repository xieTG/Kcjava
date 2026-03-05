package com.xietg.kc.db.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/*
 * CREATE TABLE IF NOT EXISTS LC (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT UNIQUE NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  questionnaires_id UUID REFERENCES questionnaires(id) ON DELETE CASCADE
);
 */
@Entity
@Table(name = "LC")

public class LCEntity {
	
	@Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = true)
    private UUID questionnaires_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;



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
    
    public UUID getQuestionnaireId() {
    	return questionnaires_id;
    }
    
    public void setQuestionnaireId(UUID id) {
    	this.questionnaires_id =id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    

}
