package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long documentId;
    private Long subjectId;
    private String subjectName;
    private String fileName;
    private String fileType;
    private String filePath;

    private String extractedText;

    // ✅ Thêm 3 fields mới
    private String status;
    private Timestamp lastViewedAt;
    private Timestamp lastEditedAt;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String moderationRiskLevel;
    private Boolean hasViolation;
    private String moderationSummary;
    private String moderationWarning;
}