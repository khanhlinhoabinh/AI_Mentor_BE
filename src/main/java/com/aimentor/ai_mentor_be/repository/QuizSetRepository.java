package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.QuizSet;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {
    List<QuizSet> findByUserOrderByCreatedAtDesc(User user);
    List<QuizSet> findByUserAndSubjectSubjectIdOrderByCreatedAtDesc(User user, Long subjectId);
}