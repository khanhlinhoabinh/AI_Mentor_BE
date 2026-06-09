// src/main/java/com/aimentor/ai_mentor_be/dto/AnnotationRequest.java
package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

@Data
public class AnnotationRequest {
    private String annotationsJson; // JSON string từ frontend
}