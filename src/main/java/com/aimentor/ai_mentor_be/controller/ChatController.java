package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.ChatRequest;
import com.aimentor.ai_mentor_be.dto.ChatResponse;
import com.aimentor.ai_mentor_be.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{conversationId}")
    public ChatResponse chat(
            @PathVariable Long conversationId,
            @RequestBody ChatRequest request
    ) {

        String result =
                chatService.chat(
                        conversationId,
                        request.getMessage()
                );

        return new ChatResponse(result);
    }
}