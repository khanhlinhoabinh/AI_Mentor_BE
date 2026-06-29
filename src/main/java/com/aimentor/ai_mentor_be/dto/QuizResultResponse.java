package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizResultResponse {
    private Double score;
    private Integer totalQuestions;
    private Integer correctCount;
    private Double percentage;
    private List<QuizQuestionDTO> questions; // kèm đáp án đúng
    private String answersJson;              // đáp án user
}