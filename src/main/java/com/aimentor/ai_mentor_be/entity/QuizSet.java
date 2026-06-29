package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "quiz_sets")
public class QuizSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nullable — không bắt buộc liên kết môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false, length = 255)
    private String title;

    // MULTIPLE_CHOICE | TRUE_FALSE
    @Column(nullable = false, length = 30)
    private String questionType;

    // EASY | MEDIUM | HARD
    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(nullable = false)
    private Integer questionCount;

    // Giây
    @Column(nullable = false)
    private Integer timeLimitSeconds;

    // Điểm mỗi câu
    @Column(nullable = false)
    private Double pointsPerQuestion;

    // Xáo trộn câu hỏi
    @Column(nullable = false)
    @Builder.Default
    private Boolean shuffle = false;

    @Column(nullable = false)
    @Builder.Default
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
}