package com.aimentor.ai_mentor_be.dto;

import lombok.*;
import java.sql.Timestamp;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizSetResponse {
    private Long id;
    private String title;
    private Long subjectId;
    private String subjectName;
    private String questionType;
    private String difficulty;
    private Integer questionCount;
    private Integer timeLimitSeconds;
    private Double pointsPerQuestion;
    private Boolean shuffle;
    private Timestamp createdAt;
    private Integer actualQuestionCount; // số câu thực tế đã tạo
    private QuizAttemptResponse lastAttempt; // lần làm gần nhất
}