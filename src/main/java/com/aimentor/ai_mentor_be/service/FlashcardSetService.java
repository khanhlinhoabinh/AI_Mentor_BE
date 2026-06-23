package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.flashcard.*;
import com.aimentor.ai_mentor_be.entity.FlashcardSet;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;
import com.aimentor.ai_mentor_be.repository.FlashcardSetRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.repository.SubjectRepository;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardSetService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    public FlashcardSetResponse create(
            UUID userId,
            CreateFlashcardSetRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject = null;

        if (request.getSubjectId() != null) {

            subject = subjectRepository.findById(
                    request.getSubjectId()
            ).orElseThrow(() ->
                    new RuntimeException("Subject not found"));
        }

        FlashcardSet flashcardSet = FlashcardSet.builder()
                .createdBy(user)
                .subject(subject)
                .setName(request.getSetName())
                .description(request.getDescription())
                .totalCards(0)
                .build();

        return mapToResponse(
                flashcardSetRepository.save(flashcardSet)
        );
    }

    public List<FlashcardSetResponse> getAll(
            UUID userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return flashcardSetRepository
                .findByCreatedByOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FlashcardSetResponse getDetail(
            UUID userId,
            Long setId
    ) {

        FlashcardSet flashcardSet =
                getOwnedFlashcardSet(userId, setId);

        return mapToResponse(flashcardSet);
    }

    public FlashcardSetResponse update(
            UUID userId,
            Long setId,
            UpdateFlashcardSetRequest request
    ) {

        FlashcardSet flashcardSet =
                getOwnedFlashcardSet(userId, setId);

        flashcardSet.setSetName(
                request.getSetName()
        );

        flashcardSet.setDescription(
                request.getDescription()
        );

        return mapToResponse(
                flashcardSetRepository.save(flashcardSet)
        );
    }

    public void delete(
            UUID userId,
            Long setId
    ) {

        FlashcardSet flashcardSet =
                getOwnedFlashcardSet(userId, setId);

        flashcardSetRepository.delete(flashcardSet);
    }

    public List<FlashcardSetResponse> search(
            UUID userId,
            String keyword
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return flashcardSetRepository
                .findByCreatedByAndSetNameContainingIgnoreCaseOrderByCreatedAtDesc(
                        user,
                        keyword
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private FlashcardSet getOwnedFlashcardSet(
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

    private FlashcardSetResponse mapToResponse(
            FlashcardSet flashcardSet
    ) {

        return FlashcardSetResponse.builder()
                .flashcardSetId(
                        flashcardSet.getFlashcardSetId()
                )
                .subjectId(
                        flashcardSet.getSubject() != null
                                ? flashcardSet.getSubject().getSubjectId()
                                : null
                )
                .setName(
                        flashcardSet.getSetName()
                )
                .description(
                        flashcardSet.getDescription()
                )
                .totalCards(
                        flashcardSet.getTotalCards()
                )
                .sourceType(
                        flashcardSet.getSourceType()
                )
                .createdAt(
                        flashcardSet.getCreatedAt()
                )
                .build();
    }
    public FlashcardSetResponse updateSourceType(
            UUID userId,
            Long setId,
            UpdateFlashcardSourceRequest request
    ) {

        FlashcardSet flashcardSet =
                getOwnedFlashcardSet(
                        userId,
                        setId
                );

        flashcardSet.setSourceType(
                request.getSourceType()
        );

        return mapToResponse(
                flashcardSetRepository.save(
                        flashcardSet
                )
        );
    }
}