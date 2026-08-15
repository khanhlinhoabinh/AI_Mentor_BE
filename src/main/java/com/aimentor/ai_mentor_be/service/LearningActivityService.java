package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.RecordFlashcardStudyRequest;
import com.aimentor.ai_mentor_be.entity.FlashcardSet;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LearningActivityService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final ActivityLogService activityLogService;

    public void recordFlashcardStudy(
            User user,
            RecordFlashcardStudyRequest request
    ) {
        if (request.getFlashcardSetId() == null) {
            throw new RuntimeException("flashcardSetId is required");
        }

        if (request.getCardsReviewed() == null || request.getCardsReviewed() < 0) {
            throw new RuntimeException("cardsReviewed must be >= 0");
        }

        FlashcardSet flashcardSet =
                flashcardSetRepository.findById(request.getFlashcardSetId())
                        .orElseThrow(() ->
                                new RuntimeException("Flashcard set not found"));

        if (!flashcardSet.getCreatedBy()
                .getUserId()
                .equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        int duration = request.getDurationSeconds() == null
                ? 0
                : Math.max(0, request.getDurationSeconds());

        String description =
                "Học flashcard: " + flashcardSet.getSetName()
                        + " | cardsReviewed=" + request.getCardsReviewed()
                        + " | durationSeconds=" + duration;

        activityLogService.saveLog(
                user,
                "STUDY_FLASHCARD",
                description
        );
    }
}