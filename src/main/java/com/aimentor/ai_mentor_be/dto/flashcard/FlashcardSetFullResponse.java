package com.aimentor.ai_mentor_be.dto.flashcard;

import lombok.*;

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

    private String sourceType;

    private LocalDateTime createdAt;

    private List<FlashcardResponse> cards;
}