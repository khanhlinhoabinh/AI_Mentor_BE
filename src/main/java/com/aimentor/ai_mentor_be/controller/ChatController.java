package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.ChatRequest;
import com.aimentor.ai_mentor_be.dto.ChatResponse;
import com.aimentor.ai_mentor_be.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // Chat thường — giữ nguyên
    @PostMapping("/{conversationId}")
    public ChatResponse chat(
            @PathVariable Long conversationId,
            @RequestBody ChatRequest request
    ) {
        String result = chatService.chat(conversationId, request.getMessage());
        return new ChatResponse(result);
    }

    // ✅ Chat với file đính kèm
    @PostMapping(value = "/{conversationId}/file",
            consumes = "multipart/form-data")
    public ChatResponse chatWithFile(
            @PathVariable Long conversationId,
            @RequestParam("message") String message,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String result = chatService.chatWithFile(conversationId, message, file);
        return new ChatResponse(result);
    }
}