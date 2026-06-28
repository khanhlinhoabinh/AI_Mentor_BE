package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.sql.Timestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizAttemptResponse {
    private Long id;
    private Double score;
    private Integer totalQuestions;
    private Integer correctCount;
    private String answersJson;
    private Timestamp attemptedAt;
}