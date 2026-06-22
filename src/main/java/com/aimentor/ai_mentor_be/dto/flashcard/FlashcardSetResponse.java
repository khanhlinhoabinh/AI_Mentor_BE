package com.aimentor.ai_mentor_be.dto.flashcard;

import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetResponse {

    private Long flashcardSetId;

    private Long subjectId;

    private String setName;

    private String description;

    private Integer totalCards;

    private FlashcardSourceType sourceType;

    private LocalDateTime createdAt;
}