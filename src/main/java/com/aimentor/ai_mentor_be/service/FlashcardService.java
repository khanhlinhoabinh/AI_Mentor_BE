package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.flashcard.*;
import com.aimentor.ai_mentor_be.entity.Flashcard;
import com.aimentor.ai_mentor_be.entity.FlashcardSet;
import com.aimentor.ai_mentor_be.repository.FlashcardRepository;
import com.aimentor.ai_mentor_be.repository.FlashcardSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;

    public FlashcardResponse create(
            UUID userId,
            Long setId,
            CreateFlashcardRequest request
    ) {

        FlashcardSet flashcardSet =
                getOwnedSet(userId, setId);

        if (flashcardSet.getSourceType() == null) {

            throw new RuntimeException(
                    "Please select flashcard creation method first"
            );
        }
        if (flashcardSet.getSourceType()
                != FlashcardSourceType.MANUAL) {

            throw new RuntimeException(
                    "This flashcard set is not manual type"
            );
        }
        int nextOrder =
                (int) flashcardRepository
                        .countByFlashcardSet_FlashcardSetId(setId) + 1;

        Flashcard flashcard = Flashcard.builder()
                .flashcardSet(flashcardSet)
                .cardType(request.getCardType())
                .frontContent(request.getFrontContent())
                .backContent(request.getBackContent())
                .displayOrder(nextOrder)
                .build();

        Flashcard saved =
                flashcardRepository.save(flashcard);

        flashcardSet.setTotalCards(
                flashcardSet.getTotalCards() + 1
        );

        flashcardSetRepository.save(flashcardSet);

        return mapToResponse(saved);
    }

    public List<FlashcardResponse> getBySet(
            UUID userId,
            Long setId
    ) {

        getOwnedSet(userId, setId);

        return flashcardRepository
                .findByFlashcardSet_FlashcardSetIdOrderByDisplayOrderAsc(setId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FlashcardResponse getDetail(
            UUID userId,
            Long cardId
    ) {

        Flashcard flashcard =
                getOwnedCard(userId, cardId);

        return mapToResponse(flashcard);
    }

    public FlashcardResponse update(
            UUID userId,
            Long cardId,
            UpdateFlashcardRequest request
    ) {

        Flashcard flashcard =
                getOwnedCard(userId, cardId);

        flashcard.setCardType(
                request.getCardType()
        );

        flashcard.setFrontContent(
                request.getFrontContent()
        );

        flashcard.setBackContent(
                request.getBackContent()
        );

        return mapToResponse(
                flashcardRepository.save(flashcard)
        );
    }

    public void delete(
            UUID userId,
            Long cardId
    ) {

        Flashcard flashcard =
                getOwnedCard(userId, cardId);

        FlashcardSet flashcardSet =
                flashcard.getFlashcardSet();

        flashcardRepository.delete(flashcard);

        flashcardSet.setTotalCards(
                Math.max(
                        0,
                        flashcardSet.getTotalCards() - 1
                )
        );

        flashcardSetRepository.save(flashcardSet);
    }

    private Flashcard getOwnedCard(
            UUID userId,
            Long cardId
    ) {

        Flashcard flashcard =
                flashcardRepository.findById(cardId)
                        .orElseThrow(() ->
                                new RuntimeException("Flashcard not found"));

        if (!flashcard.getFlashcardSet()
                .getCreatedBy()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        return flashcard;
    }

    private FlashcardSet getOwnedSet(
            UUID userId,
            Long setId
    ) {

        FlashcardSet flashcardSet =
                flashcardSetRepository.findById(setId)
                        .orElseThrow(() ->
                                new RuntimeException("Flashcard set not found"));

        if (!flashcardSet.getCreatedBy()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        return flashcardSet;
    }

    private FlashcardResponse mapToResponse(
            Flashcard flashcard
    ) {

        return FlashcardResponse.builder()
                .flashcardId(
                        flashcard.getFlashcardId()
                )
                .flashcardSetId(
                        flashcard.getFlashcardSet()
                                .getFlashcardSetId()
                )
                .cardType(
                        flashcard.getCardType()
                )
                .frontContent(
                        flashcard.getFrontContent()
                )
                .backContent(
                        flashcard.getBackContent()
                )
                .displayOrder(
                        flashcard.getDisplayOrder()
                )
                .createdAt(
                        flashcard.getCreatedAt()
                )
                .build();
    }

}