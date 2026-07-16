package com.aimentor.ai_mentor_be.dto;

import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {

    private Long activityId;

    private String fullName;

    private String action;

    private String description;

    private Timestamp createdAt;

}