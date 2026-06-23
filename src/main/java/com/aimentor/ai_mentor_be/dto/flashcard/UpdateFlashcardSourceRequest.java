package com.aimentor.ai_mentor_be.dto.flashcard;

import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;
import lombok.Data;

@Data
public class UpdateFlashcardSourceRequest {

    private FlashcardSourceType sourceType;

}