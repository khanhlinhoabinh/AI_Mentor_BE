package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.RoadmapStatus;
import lombok.Data;

@Data
public class UpdateMilestoneStatusRequest {

    private RoadmapStatus status;
}