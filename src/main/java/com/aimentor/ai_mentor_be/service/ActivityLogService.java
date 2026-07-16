package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.ActivityLogResponse;
import com.aimentor.ai_mentor_be.entity.ActivityLog;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    private final ActivityLogSSEService activityLogSSEService;

    public void saveLog(
            User user,
            String action,
            String description
    ) {

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .description(description)
                .build();

        ActivityLog saved = repository.save(log);

        ActivityLogResponse response = ActivityLogResponse.builder()
                .activityId(saved.getActivityId())
                .fullName(saved.getUser().getFullName())
                .action(saved.getAction())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .build();

        activityLogSSEService.send(response);
    }

    public List<ActivityLogResponse> getLatestLogs() {

        return repository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> ActivityLogResponse.builder()
                        .activityId(log.getActivityId())
                        .fullName(log.getUser().getFullName())
                        .action(log.getAction())
                        .description(log.getDescription())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }

}