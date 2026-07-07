package com.aimentor.ai_mentor_be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoodFeedbackRequest {

    /**
     * HAPPY
     * NORMAL
     * SAD
     */
    private String mood;

}