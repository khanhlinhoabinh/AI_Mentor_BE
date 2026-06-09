// src/main/java/com/aimentor/ai_mentor_be/entity/PdfAnnotation.java
package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "pdf_annotations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long annotationId;

    // Liên kết tới document nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Liên kết tới user nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Toàn bộ annotations lưu dạng JSON string
    // Ví dụ: [{"page":1,"type":"highlight","color":"#ffff00","rects":[...]}, ...]
    @Column(columnDefinition = "LONGTEXT")
    private String annotationsJson;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}