package com.aimentor.ai_mentor_be.dto;

import lombok.Builder;
import lombok.Data;
import com.aimentor.ai_mentor_be.entity.RoadmapStatus;

import java.time.LocalDate;

@Data
@Builder
public class RoadmapMilestoneResponse {

    private Long milestoneId;

    private Long taskId;

    private String milestoneTitle;

    private LocalDate dueDate;

    private RoadmapStatus status;
}