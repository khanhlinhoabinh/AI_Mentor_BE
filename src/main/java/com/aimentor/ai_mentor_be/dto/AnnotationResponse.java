// src/main/java/com/aimentor/ai_mentor_be/dto/AnnotationResponse.java
package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.sql.Timestamp;

@Data
@Builder
public class AnnotationResponse {
    private Long annotationId;
    private Long documentId;
    private String annotationsJson;
    private Timestamp updatedAt;
}