package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.ChatMessageRepository;
import com.aimentor.ai_mentor_be.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiService geminiService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    // ── Chat thường (giữ nguyên) ──
    public String chat(Long conversationId, String message) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        ChatMessage userMessage = ChatMessage.builder()
                .conversation(conversation)
                .sender(MessageSender.USER)
                .content(message)
                .build();
        chatMessageRepository.save(userMessage);

        List<ChatMessage> messages = chatMessageRepository
                .findTop10ByConversationConversationIdOrderByCreatedAtDesc(conversationId);
        Collections.reverse(messages);

        String prompt = buildPrompt(messages, null, null);
        String answer = geminiService.chat(prompt);

        chatMessageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .sender(MessageSender.AI)
                .content(answer)
                .build());

        return answer;
    }

    // ── Chat với file đính kèm ──
    public String chatWithFile(
            Long conversationId,
            String message,
            MultipartFile file
    ) throws IOException {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Extract text từ file
        String fileText = extractTextFromFile(file);
        String fileName = file.getOriginalFilename();

        // Lưu tin nhắn user (ghi rõ đã gửi file)
        String userContent = "[Đính kèm file: " + fileName + "]\n" + message;
        chatMessageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .sender(MessageSender.USER)
                .content(userContent)
                .build());

        // Lấy lịch sử hội thoại
        List<ChatMessage> messages = chatMessageRepository
                .findTop10ByConversationConversationIdOrderByCreatedAtDesc(conversationId);
        Collections.reverse(messages);

        // Build prompt với nội dung file
        String prompt = buildPrompt(messages, fileText, fileName);
        String answer = geminiService.chat(prompt);

        chatMessageRepository.save(ChatMessage.builder()
                .conversation(conversation)
                .sender(MessageSender.AI)
                .content(answer)
                .build());

        return answer;
    }

    // ── Build prompt ──
    private String buildPrompt(
            List<ChatMessage> history,
            String fileContent,
            String fileName
    ) {
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
""");

        // Thêm nội dung file nếu có
        if (fileContent != null && !fileContent.isBlank()) {
            prompt.append("\n=== NỘI DUNG TÀI LIỆU ĐÍNH KÈM: ")
                    .append(fileName)
                    .append(" ===\n")
                    .append(fileContent, 0, Math.min(fileContent.length(), 8000))
                    .append("\n=== HẾT TÀI LIỆU ===\n");
        }

        prompt.append("\n=== LỊCH SỬ HỘI THOẠI ===\n");
        for (ChatMessage msg : history) {
            prompt.append(msg.getSender())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        return prompt.toString();
    }

    // ── Extract text từ PDF hoặc DOCX ──
    private String extractTextFromFile(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase() : "";

        if (name.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(doc);
            }
        }

        if (name.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                StringBuilder sb = new StringBuilder();
                for (XWPFParagraph p : doc.getParagraphs()) {
                    sb.append(p.getText()).append("\n");
                }
                return sb.toString();
            }
        }

        // File text thường (.txt, .md, ...)
        return new String(file.getBytes());
    }
}