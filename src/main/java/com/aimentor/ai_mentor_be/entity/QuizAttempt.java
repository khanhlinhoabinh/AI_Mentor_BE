package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctCount;

    // JSON lưu đáp án user đã chọn
    @Column(columnDefinition = "LONGTEXT")
    private String answersJson;

    @Column(nullable = false)
    @Builder.Default
    private Timestamp attemptedAt = new Timestamp(System.currentTimeMillis());
}