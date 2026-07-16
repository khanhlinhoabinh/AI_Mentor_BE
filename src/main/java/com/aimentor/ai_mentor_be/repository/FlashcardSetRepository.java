package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.FlashcardSet;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;
public interface FlashcardSetRepository
        extends JpaRepository<FlashcardSet, Long> {

    List<FlashcardSet> findByCreatedByOrderByCreatedAtDesc(
            User user
    );

    List<FlashcardSet> findByCreatedByAndSetNameContainingIgnoreCaseOrderByCreatedAtDesc(
            User user,
            String keyword
    );
    long countByCreatedBy(User user);
    long countByCreatedAtAfter(LocalDateTime time);
}