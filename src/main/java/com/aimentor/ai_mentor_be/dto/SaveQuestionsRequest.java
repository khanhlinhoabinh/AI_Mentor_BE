package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaveQuestionsRequest {
    private List<QuizQuestionDTO> questions;
}