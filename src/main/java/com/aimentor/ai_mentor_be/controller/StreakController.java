package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.LeaderboardResponse;
import com.aimentor.ai_mentor_be.dto.StreakResponse;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // Lấy thông tin chuỗi hiện tại
    @GetMapping
    public ResponseEntity<StreakResponse> getStreak() {
        return ResponseEntity.ok(
                streakService.getStreak(getCurrentUser())
        );
    }

    // Điểm danh
    @PostMapping("/check-in")
    public ResponseEntity<StreakResponse> checkIn() {
        return ResponseEntity.ok(
                streakService.checkIn(getCurrentUser())
        );
    }

    // Bảng xếp hạng — Top 50 chuỗi kỷ lục cao nhất
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard() {
        return ResponseEntity.ok(
                streakService.getLeaderboard(getCurrentUser())
        );
    }
}