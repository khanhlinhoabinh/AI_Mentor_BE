package com.aimentor.ai_mentor_be.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreakResponse {

    private Integer currentStreak;
    private Integer longestStreak;
    private Integer totalCheckIns;
    private LocalDate lastCheckInDate;
    private LocalDateTime lastCheckInAt;

    // Đã điểm danh hôm nay chưa (FE dùng để disable nút)
    private Boolean checkedInToday;

    // Danh hiệu hiện tại (tính động)
    private String badgeTitle;
    private String badgeIcon;
    private String badgeLevel; // NONE | BRONZE | SILVER | GOLD

    // Danh sách tất cả danh hiệu + trạng thái đạt được chưa
    private List<BadgeInfo> badges;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BadgeInfo {
        private String title;
        private String icon;
        private String level;
        private Integer requiredDays; // số ngày cần đạt
        private Boolean achieved;     // đã đạt chưa
        private String description;
    }
}