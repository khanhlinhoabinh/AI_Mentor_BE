package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.RoadmapTaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateRoadmapTaskRequest {

    private String taskName;

    private String taskGoal;

    private LocalDate startDate;

    private LocalDate endDate;

    private RoadmapTaskStatus status;
}