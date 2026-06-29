package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.QuizQuestion;
import com.aimentor.ai_mentor_be.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizSetOrderByOrderIndex(QuizSet quizSet);
    void deleteByQuizSet(QuizSet quizSet);
}