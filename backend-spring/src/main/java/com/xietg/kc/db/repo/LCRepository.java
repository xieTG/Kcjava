package com.xietg.kc.db.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xietg.kc.db.entity.LCEntity;




public interface LCRepository extends JpaRepository<LCEntity, UUID> {
    List<LCEntity> findByName(String name);
    
    List<LCEntity> findByNameAndYear(String name, Integer year);
}