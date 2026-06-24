package com.aimentor.ai_mentor_be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateMilestoneRequest {

    private String milestoneTitle;

    private LocalDate dueDate;
}