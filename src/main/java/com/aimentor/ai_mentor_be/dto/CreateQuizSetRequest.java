package com.aimentor.ai_mentor_be.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateQuizSetRequest {
    private String title;
    private Long subjectId;          // nullable
    private String questionType;     // MULTIPLE_CHOICE | TRUE_FALSE
    private String difficulty;       // EASY | MEDIUM | HARD
    private Integer questionCount;
    private Integer timeLimitSeconds;
    private Double pointsPerQuestion;
    private Boolean shuffle;
}