package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateConversationRequest;
import com.aimentor.ai_mentor_be.dto.CreateConversationResponse;
import com.aimentor.ai_mentor_be.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}