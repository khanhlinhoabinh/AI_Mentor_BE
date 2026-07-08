package com.aimentor.ai_mentor_be.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateReminderRequest {

    private String title;

    private String description;

    private LocalDate reminderDate;

}