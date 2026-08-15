package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.QuizAttempt;
import com.aimentor.ai_mentor_be.entity.QuizSet;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Lấy lần làm gần nhất — vẫn giữ để các API hiện tại (hiển thị lastAttempt) hoạt động
    Optional<QuizAttempt> findTopByQuizSetAndUserOrderByAttemptedAtDesc(
            QuizSet quizSet, User user);

    // ✅ MỚI — lịch sử TẤT CẢ lần làm quiz trong khoảng thời gian, dùng để AI phân tích xu hướng điểm
    List<QuizAttempt> findByUserAndAttemptedAtBetweenOrderByAttemptedAtAsc(
            User user,
            Timestamp from,
            Timestamp to
    );

    void deleteByQuizSet(QuizSet quizSet);
}