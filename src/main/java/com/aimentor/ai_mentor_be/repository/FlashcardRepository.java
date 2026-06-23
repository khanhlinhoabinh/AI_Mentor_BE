package com.aimentor.ai_mentor_be.repository;
import com.aimentor.ai_mentor_be.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface FlashcardRepository
        extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByFlashcardSet_FlashcardSetIdOrderByDisplayOrderAsc(
            Long flashcardSetId
    );

    long countByFlashcardSet_FlashcardSetId(
            Long flashcardSetId
    );
}