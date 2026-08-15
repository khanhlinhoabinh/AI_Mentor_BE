package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.ActivityLog;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();

    // ✅ MỚI — lấy toàn bộ hoạt động của 1 user trong khoảng thời gian, dùng cho đánh giá học tập AI
    List<ActivityLog> findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(
            User user,
            Timestamp from,
            Timestamp to
    );

    // ✅ MỚI — đếm số lần thực hiện 1 loại hành động trong khoảng thời gian (dự phòng cho các thống kê sau này)
    long countByUserAndActionAndCreatedAtBetween(
            User user,
            String action,
            Timestamp from,
            Timestamp to
    );
}