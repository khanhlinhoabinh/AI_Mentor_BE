package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.ModerationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final GeminiService geminiService;
    private final ObjectMapper  objectMapper = new ObjectMapper();

    public ModerationResult moderate(String textContent) {

        if (textContent == null || textContent.isBlank()) {
            log.info("Moderation skipped: empty content");
            return safeModerationResult();
        }

        // Giới hạn 6000 ký tự
        String limited = textContent.length() > 6000
                ? textContent.substring(0, 6000) + "\n...(nội dung bị cắt)"
                : textContent;

        String prompt = buildPrompt(limited);

        try {
            String raw = geminiService.chat(prompt);

            // ✅ Log để debug
            log.info("=== MODERATION RAW RESPONSE ===\n{}", raw);

            // ✅ Clean kỹ hơn — xử lý nhiều dạng markdown Gemini trả về
            String cleaned = raw
                    .replaceAll("(?s)```json\\s*", "")  // xóa ```json
                    .replaceAll("(?s)```\\s*", "")       // xóa ```
                    .trim();

            // ✅ Tìm JSON object trong response nếu có text thừa trước/sau
            int start = cleaned.indexOf("{");
            int end   = cleaned.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            log.info("=== MODERATION CLEANED JSON ===\n{}", cleaned);

            ModerationResult result = objectMapper.readValue(cleaned, ModerationResult.class);

            log.info("=== MODERATION RESULT === safe={}, riskLevel={}, hasViolation={}",
                    result.getSafe(), result.getRiskLevel(), result.getHasViolation());

            return result;

        } catch (Exception e) {
            // ✅ Log lỗi cụ thể để biết nguyên nhân
            log.error("=== MODERATION ERROR === {}: {}", e.getClass().getSimpleName(), e.getMessage());
            return safeModerationResult();
        }
    }

    private ModerationResult safeModerationResult() {
        return ModerationResult.builder()
                .safe(true)
                .riskLevel("SAFE")
                .hasViolation(false)
                .categories(List.of())
                .summary("Không thể phân tích nội dung.")
                .warning("")
                .build();
    }

    private String buildPrompt(String content) {
        return """
Bạn là một hệ thống AI kiểm duyệt nội dung tài liệu.
Nhiệm vụ:
Phân tích nội dung tài liệu được cung cấp và xác định xem tài liệu có vi phạm tiêu chuẩn cộng đồng hay không.
Các tiêu chí cần kiểm tra:
1. Nội dung 18+
- Nội dung khiêu dâm, mô tả hành vi tình dục, nội dung người lớn
2. Ngôn từ không chuẩn mực
- Chửi tục, xúc phạm, lăng mạ, kỳ thị, ngôn ngữ thô tục
3. Nội dung bạo lực cực đoan
- Khuyến khích giết người, tra tấn
4. Nội dung nguy hiểm
- Hướng dẫn tự tử, chế tạo chất nổ, phạm tội, ma túy
5. Nội dung thù ghét
- Phân biệt chủng tộc, giới tính, kích động thù hằn
6. Các nội dung không phù hợp trong môi trường học tập.
Nếu tài liệu chỉ chứa ví dụ học thuật hoặc trích dẫn giáo dục thì KHÔNG coi là vi phạm.
Chỉ trả về JSON, không giải thích, không markdown, không ký tự thừa.
Nếu an toàn:
{"safe":true,"riskLevel":"NONE","hasViolation":false,"categories":[],"summary":"Không phát hiện nội dung vi phạm.","warning":""}
Nếu vi phạm:
{"safe":false,"riskLevel":"HIGH","hasViolation":true,"categories":["PROFANITY"],"summary":"Phát hiện ngôn từ tục tĩu.","warning":"Tài liệu vi phạm tiêu chuẩn cộng đồng."}

=== NỘI DUNG TÀI LIỆU ===
""" + content;
    }
}