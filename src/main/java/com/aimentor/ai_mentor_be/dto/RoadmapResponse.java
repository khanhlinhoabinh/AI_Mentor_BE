package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.RoadmapStatus;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Builder
public class RoadmapResponse {

    private Long roadmapId;

    private Long subjectId;

    private String subjectName;

    private String roadmapTitle;

    private String learningGoal;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double progressPercent;

    private RoadmapStatus status;

    private Timestamp createdAt;
}