package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.entity.Conversation;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.ConversationRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Long createConversation(
            UUID userId,
            String title
    ) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        Conversation conversation =
                Conversation.builder()
                        .title(title)
                        .user(user)
                        .build();

        conversation =
                conversationRepository.save(conversation);

        return conversation.getConversationId();
    }
}