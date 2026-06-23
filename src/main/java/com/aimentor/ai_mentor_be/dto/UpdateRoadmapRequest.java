package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateRoadmapRequest {

    private String roadmapTitle;

    private String learningGoal;

    private LocalDate startDate;

    private LocalDate endDate;
}