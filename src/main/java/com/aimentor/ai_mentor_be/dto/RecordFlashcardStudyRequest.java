package com.aimentor.ai_mentor_be.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordFlashcardStudyRequest {

    /** Bộ flashcard mà người dùng vừa học. */
    private Long flashcardSetId;

    /** Số thẻ đã xem/ôn trong phiên học. */
    private Integer cardsReviewed;

    /** Thời gian học của phiên, tính bằng giây. Có thể null nếu FE chưa đo thời gian. */
    private Integer durationSeconds;
}