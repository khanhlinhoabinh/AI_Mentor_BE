package com.aimentor.ai_mentor_be.dto.flashcard;

import lombok.*;
import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetFullResponse {

    private Long flashcardSetId;

    private Long subjectId;

    private String setName;

    private String description;

    private Integer totalCards;

    private FlashcardSourceType sourceType;

    private LocalDateTime createdAt;

    private List<FlashcardResponse> cards;
}