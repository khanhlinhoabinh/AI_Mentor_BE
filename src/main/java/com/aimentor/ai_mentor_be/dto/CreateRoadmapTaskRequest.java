package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRoadmapTaskRequest {

    private Long roadmapId;

    private String taskName;

    private String taskGoal;

    private LocalDate startDate;

    private LocalDate endDate;
}