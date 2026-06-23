package com.aimentor.ai_mentor_be.dto.flashcard;

import com.aimentor.ai_mentor_be.entity.enums.FlashcardType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardResponse {

    private Long flashcardId;

    private Long flashcardSetId;

    private FlashcardType cardType;

    private String frontContent;

    private String backContent;

    private Integer displayOrder;

    private LocalDateTime createdAt;
}