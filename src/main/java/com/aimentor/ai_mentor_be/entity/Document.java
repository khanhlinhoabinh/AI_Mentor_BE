package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_subject_document", columnList = "subject_id")
        }
)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Lob
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "UPLOADED";

    @Column(name = "last_viewed_at")
    private Timestamp lastViewedAt;

    @Column(name = "last_edited_at")
    private Timestamp lastEditedAt;

    // ===== MODERATION FIELDS =====
    @Column(name = "moderation_risk_level", length = 20)
    @Builder.Default
    private String moderationRiskLevel = "SAFE";

    @Column(name = "has_violation")
    @Builder.Default
    private Boolean hasViolation = false;

    @Column(name = "moderation_summary", columnDefinition = "TEXT")
    private String moderationSummary;

    @Column(name = "moderation_warning", columnDefinition = "TEXT")
    private String moderationWarning;
    // ===== END MODERATION FIELDS =====

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "UPLOADED";
        if (this.moderationRiskLevel == null) this.moderationRiskLevel = "SAFE";
        if (this.hasViolation == null) this.hasViolation = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}