package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.entity.UserStreak;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUser(User user);

    Optional<UserStreak> findByUserUserId(UUID userId);

    /**
     * Top N người có longestStreak cao nhất.
     * Cùng điểm → ai đạt TRƯỚC (longestStreakAt nhỏ hơn) xếp trên.
     * JOIN FETCH user để tránh N+1 query khi map fullName/avatarUrl.
     */
    @Query("SELECT us FROM UserStreak us JOIN FETCH us.user " +
            "WHERE us.longestStreak > 0 " +
            "ORDER BY us.longestStreak DESC, us.longestStreakAt ASC")
    List<UserStreak> findTopStreaks(Pageable pageable);

    /**
     * Đếm số người xếp hạng CAO HƠN 1 mốc (streak, achievedAt) cho trước
     * → dùng để tính hạng thật của 1 user kể cả khi họ nằm ngoài Top 50.
     */
    @Query("SELECT COUNT(us) FROM UserStreak us WHERE us.longestStreak > 0 " +
            "AND (us.longestStreak > :streak " +
            "     OR (us.longestStreak = :streak AND us.longestStreakAt < :achievedAt))")
    long countBetterThan(@Param("streak") Integer streak, @Param("achievedAt") LocalDateTime achievedAt);

    long countByLongestStreakGreaterThan(Integer streak);
}