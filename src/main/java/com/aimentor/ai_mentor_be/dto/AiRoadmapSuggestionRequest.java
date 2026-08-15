package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AiRoadmapSuggestionRequest {

    private String roadmapTitle;

    private String topic;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;
}