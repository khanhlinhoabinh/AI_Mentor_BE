// src/main/java/com/aimentor/ai_mentor_be/repository/PdfAnnotationRepository.java
package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.PdfAnnotation;
import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PdfAnnotationRepository
        extends JpaRepository<PdfAnnotation, Long> {

    // Tìm annotation của 1 user trên 1 document
    Optional<PdfAnnotation> findByDocumentAndUser(
            Document document, User user);
    @Transactional
    void deleteByDocument(Document document);
}