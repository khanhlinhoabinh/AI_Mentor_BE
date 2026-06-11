package com.aimentor.ai_mentor_be.dto;

import com.aimentor.ai_mentor_be.entity.MessageSender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {

    private Long messageId;
    private MessageSender sender;
    private String content;
    private LocalDateTime createdAt;
}