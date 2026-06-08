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
    private Timestamp createdAt;
    private Timestamp updatedAt;
}