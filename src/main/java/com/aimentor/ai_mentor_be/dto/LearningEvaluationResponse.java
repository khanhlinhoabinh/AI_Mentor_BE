package com.aimentor.ai_mentor_be.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningEvaluationResponse {

    private String periodLabel;
    private LocalDate fromDate;
    private LocalDate toDate;

    /** ON_TRACK | NEEDS_ATTENTION | INACTIVE */
    private String status;

    /** Điểm tổng hợp do backend tính từ hoạt động thực tế. Không phải điểm học tập tuyệt đối. */
    private Integer activityScore;

    private WeeklyMetrics metrics;

    private String aiSummary;
    private String weeklyOverview;

    private List<String> strengths;
    private List<String> concerns;
    private List<String> recommendations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyMetrics {
        private Integer activeDays;
        private Integer loginDays;

        private Integer quizAttempts;
        private Double averageQuizPercentage;
        private Double bestQuizPercentage;
        private Double latestQuizPercentage;

        private Integer flashcardStudySessions;
        private Integer flashcardsReviewed;

        private Integer overdueMilestones;
        private Integer overdueTasks;

        private Integer activeRoadmaps;
        private Double averageRoadmapProgress;

        private Integer currentStreak;
        private Integer longestStreak;
    }
}