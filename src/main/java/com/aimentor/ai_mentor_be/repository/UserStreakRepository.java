package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUser(User user);

    Optional<UserStreak> findByUserUserId(UUID userId);
}