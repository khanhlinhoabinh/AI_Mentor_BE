package com.aimentor.ai_mentor_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationResponse {

    private Long conversationId;
    private String title;
    private LocalDateTime createdAt;
}