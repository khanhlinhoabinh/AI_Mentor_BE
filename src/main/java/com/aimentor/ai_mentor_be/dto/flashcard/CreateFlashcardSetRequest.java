package com.aimentor.ai_mentor_be.dto.flashcard;

import lombok.Data;

@Data
public class CreateFlashcardSetRequest {

    private Long subjectId;

    private String setName;

    private String description;
}