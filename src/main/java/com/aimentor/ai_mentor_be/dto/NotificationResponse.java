package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationResponse {

    private UUID notificationId;

    private String title;

    private String content;

    private NotificationType type;

    private String referenceId;

    private String referenceType;

    private boolean isRead;

    private Timestamp createdAt;

}