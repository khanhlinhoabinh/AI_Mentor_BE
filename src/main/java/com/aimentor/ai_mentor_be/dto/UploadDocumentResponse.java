package com.aimentor.ai_mentor_be.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class UploadDocumentResponse {

    private Long documentId;

    private Long subjectId;

    private String fileName;

    private String fileType;

    private String filePath;

    private Timestamp createdAt;
}