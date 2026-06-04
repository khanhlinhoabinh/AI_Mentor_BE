package com.aimentor.ai_mentor_be.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class SubjectResponse {

    private Long subjectId;

    private String subjectName;

    private String description;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}