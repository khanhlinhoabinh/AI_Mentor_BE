package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.FlashcardSet;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardSetRepository
        extends JpaRepository<FlashcardSet, Long> {

    List<FlashcardSet> findByCreatedByOrderByCreatedAtDesc(
            User user
    );

    List<FlashcardSet> findByCreatedByAndSetNameContainingIgnoreCaseOrderByCreatedAtDesc(
            User user,
            String keyword
    );
}