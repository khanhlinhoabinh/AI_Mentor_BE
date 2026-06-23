package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.RoadmapTaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RoadmapTaskResponse {

    private Long taskId;

    private Long roadmapId;

    private String taskTitle;

    private String taskGoal;

    private LocalDate startDate;

    private LocalDate endDate;

    private RoadmapTaskStatus status;
}