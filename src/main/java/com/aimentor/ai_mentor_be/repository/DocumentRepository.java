package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.sql.Timestamp;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findBySubject(Subject subject);
    long countBySubject(Subject subject);

    // Đếm tài liệu theo loại file
    long countBySubjectAndFileType(Subject subject, String fileType);

    // Danh sách tài liệu do user upload
    List<Document> findByUploadedBy(User user);

    // ================= ADMIN =================

    // Đếm số tài liệu còn tồn tại trên hệ thống
    long countByStatus(String status);
    long countByUploadedBy(User user);
    long countByCreatedAtAfter(Timestamp time);

    // Lấy tất cả tài liệu có vi phạm
    List<Document> findByHasViolationTrueOrderByCreatedAtDesc();
}