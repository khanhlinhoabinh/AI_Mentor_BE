package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.entity.Conversation;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.ConversationRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import com.aimentor.ai_mentor_be.dto.ConversationResponse;
import com.aimentor.ai_mentor_be.dto.MessageResponse;
import com.aimentor.ai_mentor_be.repository.ChatMessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
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
    public List<ConversationResponse> getUserConversations(UUID userId) {

        return conversationRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> new ConversationResponse(
                        c.getConversationId(),
                        c.getTitle(),
                        c.getCreatedAt()
                ))
                .toList();
    }
    public List<MessageResponse> getMessages(Long conversationId) {

        return chatMessageRepository
                .findByConversationConversationIdOrderByCreatedAtAsc(
                        conversationId
                )
                .stream()
                .map(m -> new MessageResponse(
                        m.getMessageId(),
                        m.getSender(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .toList();
    }
    public void deleteConversation(Long conversationId) {

        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () -> new RuntimeException("Conversation not found")
                        );

        conversationRepository.delete(conversation);
    }
}