package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.ChatMessageRepository;
import com.aimentor.ai_mentor_be.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;
@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiService geminiService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public String chat(
            Long conversationId,
            String message
    ) {

        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(
                                () -> new RuntimeException("Conversation not found")
                        );
        ChatMessage userMessage =
                ChatMessage.builder()
                        .conversation(conversation)
                        .sender(MessageSender.USER)
                        .content(message)
                        .build();

        chatMessageRepository.save(userMessage);

        List<ChatMessage> messages =
                chatMessageRepository
                        .findTop10ByConversationConversationIdOrderByCreatedAtDesc(
                                conversationId
                        );

// đảo lại để từ cũ → mới
        Collections.reverse(messages);

// Ghép lịch sử thành prompt
// Ghép lịch sử thành prompt
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
Bạn là AI Mentor hỗ trợ học tập.

Nhiệm vụ:
- Giải thích kiến thức dễ hiểu.
- Hỗ trợ học tập và lập trình.
- Trả lời chính xác, ngắn gọn.
- Nếu là câu hỏi lập trình hãy đưa ví dụ.
- Nếu không chắc chắn thì nói rõ không chắc chắn.
- Trả lời bằng tiếng Việt.
- Dựa trên lịch sử hội thoại bên dưới để giữ ngữ cảnh.
                        
=== LỊCH SỬ HỘI THOẠI ===
""");
        for (ChatMessage msg : messages) {

            prompt.append(msg.getSender())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

// Gửi toàn bộ context cho Gemini
        String answer =
                geminiService.chat(
                        prompt.toString()
                );

        ChatMessage aiMessage =
                ChatMessage.builder()
                        .conversation(conversation)
                        .sender(MessageSender.AI)
                        .content(answer)
                        .build();

        chatMessageRepository.save(aiMessage);

        return answer;
    }
}