package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findBySubject(Subject subject);

    // ✅ Thêm 3 method đếm
    long countBySubject(Subject subject);

    long countBySubjectAndFileType(Subject subject, String fileType);

    List<Document> findByUploadedBy(User user);
}