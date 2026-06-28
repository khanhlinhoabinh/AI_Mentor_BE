package com.aimentor.ai_mentor_be.dto.flashcard;

import lombok.Data;

@Data
public class GenerateFlashcardRequest {

    private String setName;

    private String prompt;

    private Integer numberOfCards;

    private Long documentId;
}