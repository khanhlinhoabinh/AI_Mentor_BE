package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();

}