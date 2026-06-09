package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserUserId(UUID userId);
}