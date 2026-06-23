package com.aimentor.ai_mentor_be.dto.flashcard;

import com.aimentor.ai_mentor_be.entity.enums.FlashcardType;
import lombok.Data;

@Data
public class UpdateFlashcardRequest {

    private FlashcardType cardType;

    private String frontContent;

    private String backContent;
}