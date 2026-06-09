package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationConversationIdOrderByCreatedAtAsc(
            Long conversationId
    );
    List<ChatMessage> findTop10ByConversationConversationIdOrderByCreatedAtDesc(
            Long conversationId
    );
}