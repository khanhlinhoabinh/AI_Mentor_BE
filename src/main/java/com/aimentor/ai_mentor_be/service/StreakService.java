package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.LeaderboardResponse;
import com.aimentor.ai_mentor_be.dto.StreakResponse;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.entity.UserStreak;
import com.aimentor.ai_mentor_be.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserStreakRepository userStreakRepository;

    // ══════════════════════════════════════════
    // ⚠️ ĐỔI ĐÂY KHI MUỐN THAY ĐỔI MỐC DANH HIỆU
    // Hiện tại: 1, 2, 3 ngày (để demo nhanh)
    // Thật sự:  3, 7, 15 ngày
    // ══════════════════════════════════════════
    private static final int BADGE_1_DAYS = 1;  // TODO: đổi thành 3  khi production
    private static final int BADGE_2_DAYS = 2;  // TODO: đổi thành 7  khi production
    private static final int BADGE_3_DAYS = 3;  // TODO: đổi thành 15 khi production

    private static final String BADGE_1_TITLE = "Chú ong chăm chỉ";
    private static final String BADGE_2_TITLE = "Ngôi sao học tập";
    private static final String BADGE_3_TITLE = "Bậc thầy học tập";

    private static final String BADGE_1_ICON  = "🐝";
    private static final String BADGE_2_ICON  = "⭐";
    private static final String BADGE_3_ICON  = "🏆";

    // Số người tối đa hiển thị trên bảng xếp hạng
    private static final int LEADERBOARD_SIZE = 50;

    // ═══════════════════════════════════════
    // ĐIỂM DANH
    // ═══════════════════════════════════════
    @Transactional
    public StreakResponse checkIn(User currentUser) {

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        UserStreak streak = userStreakRepository
                .findByUser(currentUser)
                .orElseGet(() -> UserStreak.builder()
                        .user(currentUser)
                        .build());

        // Đã điểm danh hôm nay rồi → trả về thông tin hiện tại, không tính lại
        if (today.equals(streak.getLastCheckInDate())) {
            return mapToResponse(streak, true);
        }

        LocalDate yesterday = today.minusDays(1);

        if (streak.getLastCheckInDate() == null) {
            // Lần đầu điểm danh
            streak.setCurrentStreak(1);

        } else if (yesterday.equals(streak.getLastCheckInDate())) {
            // Hôm qua đã điểm danh → chuỗi tiếp tục
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);

        } else {
            // Bỏ lỡ ít nhất 1 ngày → reset chuỗi về 1
            streak.setCurrentStreak(1);
        }

        // Cập nhật kỷ lục dài nhất + THỜI ĐIỂM đạt kỷ lục (dùng để xếp hạng khi hoà điểm)
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
            streak.setLongestStreakAt(now);
        }

        streak.setLastCheckInDate(today);
        streak.setLastCheckInAt(now);
        streak.setTotalCheckIns(streak.getTotalCheckIns() + 1);
        streak.setUpdatedAt(now);

        userStreakRepository.save(streak);

        return mapToResponse(streak, true);
    }

    // ═══════════════════════════════════════
    // LẤY THÔNG TIN CHUỖI HIỆN TẠI
    // ═══════════════════════════════════════
    public StreakResponse getStreak(User currentUser) {

        UserStreak streak = userStreakRepository
                .findByUser(currentUser)
                .orElseGet(() -> UserStreak.builder()
                        .user(currentUser)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalCheckIns(0)
                        .build());

        LocalDate today = LocalDate.now();
        boolean checkedInToday = today.equals(streak.getLastCheckInDate());

        // Nếu chuỗi bị gián đoạn (hôm qua không điểm danh)
        // → reset currentStreak về 0 khi get (không lưu DB, chỉ hiển thị)
        if (streak.getLastCheckInDate() != null
                && !checkedInToday
                && !today.minusDays(1).equals(streak.getLastCheckInDate())) {

            UserStreak display = UserStreak.builder()
                    .user(currentUser)
                    .currentStreak(0)
                    .longestStreak(streak.getLongestStreak())
                    .totalCheckIns(streak.getTotalCheckIns())
                    .lastCheckInDate(streak.getLastCheckInDate())
                    .lastCheckInAt(streak.getLastCheckInAt())
                    .build();

            return mapToResponse(display, false);
        }

        return mapToResponse(streak, checkedInToday);
    }

    // ═══════════════════════════════════════
    // BẢNG XẾP HẠNG (Top 50 + hạng của chính user hiện tại)
    // ═══════════════════════════════════════
    public LeaderboardResponse getLeaderboard(User currentUser) {

        List<UserStreak> top = userStreakRepository.findTopStreaks(PageRequest.of(0, LEADERBOARD_SIZE));

        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            UserStreak us = top.get(i);
            entries.add(LeaderboardResponse.LeaderboardEntry.builder()
                    .rank(i + 1)
                    .userId(us.getUser().getUserId().toString())
                    .fullName(us.getUser().getFullName())
                    .avatarUrl(us.getUser().getAvatarUrl())
                    .longestStreak(us.getLongestStreak())
                    .currentStreak(us.getCurrentStreak())
                    .achievedAt(us.getLongestStreakAt())
                    .isCurrentUser(us.getUser().getUserId().equals(currentUser.getUserId()))
                    .build());
        }

        // Tính hạng thật của user hiện tại, kể cả khi họ KHÔNG nằm trong top 50
        UserStreak myStreak = userStreakRepository.findByUser(currentUser).orElse(null);
        Integer myRank = null;
        Integer myLongestStreak = 0;

        if (myStreak != null && myStreak.getLongestStreak() != null && myStreak.getLongestStreak() > 0) {
            myLongestStreak = myStreak.getLongestStreak();
            long betterCount = userStreakRepository.countBetterThan(
                    myStreak.getLongestStreak(), myStreak.getLongestStreakAt());
            myRank = (int) betterCount + 1;
        }

        long totalRanked = userStreakRepository.countByLongestStreakGreaterThan(0);

        return LeaderboardResponse.builder()
                .topUsers(entries)
                .myRank(myRank)
                .myLongestStreak(myLongestStreak)
                .totalRankedUsers(totalRanked)
                .build();
    }

    // ═══════════════════════════════════════
    // PRIVATE: Build response + tính danh hiệu động
    // ═══════════════════════════════════════
    private StreakResponse mapToResponse(UserStreak streak, boolean checkedInToday) {

        int days = streak.getCurrentStreak();

        String badgeTitle = null;
        String badgeIcon  = null;
        String badgeLevel = "NONE";

        if (days >= BADGE_3_DAYS) {
            badgeTitle = BADGE_3_TITLE;
            badgeIcon  = BADGE_3_ICON;
            badgeLevel = "GOLD";
        } else if (days >= BADGE_2_DAYS) {
            badgeTitle = BADGE_2_TITLE;
            badgeIcon  = BADGE_2_ICON;
            badgeLevel = "SILVER";
        } else if (days >= BADGE_1_DAYS) {
            badgeTitle = BADGE_1_TITLE;
            badgeIcon  = BADGE_1_ICON;
            badgeLevel = "BRONZE";
        }

        List<StreakResponse.BadgeInfo> badges = List.of(
                StreakResponse.BadgeInfo.builder()
                        .title(BADGE_1_TITLE)
                        .icon(BADGE_1_ICON)
                        .level("BRONZE")
                        .requiredDays(BADGE_1_DAYS)
                        .achieved(days >= BADGE_1_DAYS)
                        .description(BADGE_1_DAYS + " ngày học liên tiếp")
                        .build(),
                StreakResponse.BadgeInfo.builder()
                        .title(BADGE_2_TITLE)
                        .icon(BADGE_2_ICON)
                        .level("SILVER")
                        .requiredDays(BADGE_2_DAYS)
                        .achieved(days >= BADGE_2_DAYS)
                        .description(BADGE_2_DAYS + " ngày học liên tiếp")
                        .build(),
                StreakResponse.BadgeInfo.builder()
                        .title(BADGE_3_TITLE)
                        .icon(BADGE_3_ICON)
                        .level("GOLD")
                        .requiredDays(BADGE_3_DAYS)
                        .achieved(days >= BADGE_3_DAYS)
                        .description(BADGE_3_DAYS + " ngày học liên tiếp")
                        .build()
        );

        return StreakResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .totalCheckIns(streak.getTotalCheckIns())
                .lastCheckInDate(streak.getLastCheckInDate())
                .lastCheckInAt(streak.getLastCheckInAt())
                .checkedInToday(checkedInToday)
                .badgeTitle(badgeTitle)
                .badgeIcon(badgeIcon)
                .badgeLevel(badgeLevel)
                .badges(badges)
                .build();
    }
}