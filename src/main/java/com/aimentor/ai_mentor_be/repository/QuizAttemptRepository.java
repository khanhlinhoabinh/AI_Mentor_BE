package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.QuizAttempt;
import com.aimentor.ai_mentor_be.entity.QuizSet;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    // Lấy lần làm gần nhất
    Optional<QuizAttempt> findTopByQuizSetAndUserOrderByAttemptedAtDesc(
            QuizSet quizSet, User user);

    void deleteByQuizSet(QuizSet quizSet);
}