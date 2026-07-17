package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_streaks")
public class UserStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Chuỗi hiện tại
    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    // Chuỗi dài nhất từ trước đến nay
    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    // Ngày điểm danh gần nhất (chỉ lưu ngày, không lưu giờ)
    @Column(name = "last_check_in_date")
    private LocalDate lastCheckInDate;

    // Thời điểm điểm danh gần nhất (lưu đầy đủ để hiển thị)
    @Column(name = "last_check_in_at")
    private LocalDateTime lastCheckInAt;

    // Tổng số ngày đã điểm danh từ trước đến nay
    @Column(name = "total_check_ins", nullable = false)
    @Builder.Default
    private Integer totalCheckIns = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}