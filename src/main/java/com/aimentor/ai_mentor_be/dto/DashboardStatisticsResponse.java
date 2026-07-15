package com.aimentor.ai_mentor_be.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    private Long totalUsers;

    private Long totalDocuments;

    private Long totalQuizSets;

    private Long totalFlashcardSets;

}