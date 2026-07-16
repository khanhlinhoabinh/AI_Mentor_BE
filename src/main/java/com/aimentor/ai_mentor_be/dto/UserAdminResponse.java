package com.aimentor.ai_mentor_be.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {

    private UUID userId;

    private String fullName;

    private String email;

    private String role;

    private Boolean isActive;

    private Timestamp createdAt;

    private Timestamp lastLogin;

    private String avatarUrl;

    private Long totalSubjects;

    private Long totalDocuments;

    private Long totalQuizSets;

    private Long totalFlashcardSets;
}