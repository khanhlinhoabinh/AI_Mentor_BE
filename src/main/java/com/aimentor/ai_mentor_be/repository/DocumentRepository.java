package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findBySubject(Subject subject);
}