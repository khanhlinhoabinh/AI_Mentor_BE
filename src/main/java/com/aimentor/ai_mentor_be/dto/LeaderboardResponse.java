package com.aimentor.ai_mentor_be.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponse {

    // Top 50 người xếp hạng cao nhất
    private List<LeaderboardEntry> topUsers;

    // Hạng thật của user hiện tại (kể cả khi KHÔNG nằm trong top 50). null nếu chưa từng điểm danh.
    private Integer myRank;

    // Kỷ lục chuỗi dài nhất của user hiện tại
    private Integer myLongestStreak;

    // Tổng số người đã có ít nhất 1 kỷ lục (để FE hiển thị "Hạng X / Y người")
    private Long totalRankedUsers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaderboardEntry {
        private Integer rank;
        private String userId;
        private String fullName;
        private String avatarUrl;
        private Integer longestStreak;
        private Integer currentStreak;
        private LocalDateTime achievedAt;
        private Boolean isCurrentUser;
    }
}