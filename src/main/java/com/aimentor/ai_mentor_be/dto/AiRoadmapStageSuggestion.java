package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AiRoadmapStageSuggestion {

    private String stageName;

    private String stageGoal;

    private LocalDate startDate;

    private LocalDate endDate;
}