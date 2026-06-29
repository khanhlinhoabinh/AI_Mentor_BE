package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    // JSON string — lưu options hoặc statements tùy loại
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String optionsJson;

    // Với MULTIPLE_CHOICE: "A","B","C","D"
    // Với TRUE_FALSE: không dùng (null)
    @Column(length = 10)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    // Thứ tự câu hỏi
    @Column(nullable = false)
    private Integer orderIndex;
}