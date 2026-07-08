package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.ReminderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateReminderRequest {

    private String title;

    private String description;

    private LocalDate reminderDate;

    private ReminderStatus status;

}