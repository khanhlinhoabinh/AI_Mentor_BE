package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

@Data
public class UpdateSubjectRequest {

    private String subjectName;

    private String description;
}