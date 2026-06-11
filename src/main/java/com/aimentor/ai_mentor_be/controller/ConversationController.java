package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateConversationRequest;
import com.aimentor.ai_mentor_be.dto.CreateConversationResponse;
import com.aimentor.ai_mentor_be.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.aimentor.ai_mentor_be.dto.ConversationResponse;
import com.aimentor.ai_mentor_be.dto.MessageResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/{userId}")
    public CreateConversationResponse createConversation(
            @PathVariable UUID userId,
            @RequestBody CreateConversationRequest request
    ) {

        Long conversationId =
                conversationService.createConversation(
                        userId,
                        request.getTitle()
                );

        return new CreateConversationResponse(
                conversationId
        );
    }
    @GetMapping("/user/{userId}")
    public List<ConversationResponse> getUserConversations(
            @PathVariable UUID userId
    ) {
        return conversationService.getUserConversations(userId);
    }
    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable Long conversationId
    ) {
        return conversationService.getMessages(conversationId);
    }
    @DeleteMapping("/{conversationId}")
    public void deleteConversation(
            @PathVariable Long conversationId
    ) {
        conversationService.deleteConversation(conversationId);
    }
}