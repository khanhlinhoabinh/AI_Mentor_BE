package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.ReminderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ReminderResponse {

    private UUID reminderId;

    private String title;

    private String description;

    private LocalDate reminderDate;

    private ReminderStatus status;

    private Timestamp createdAt;

}