package com.aimentor.ai_mentor_be.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizQuestionDTO {
    private Long id;
    private String question;
    private String optionsJson;  // raw JSON string
    private String correctAnswer;
    private String explanation;
    private Integer orderIndex;
}