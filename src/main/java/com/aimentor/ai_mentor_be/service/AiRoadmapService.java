package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.AiRoadmapStageSuggestion;
import com.aimentor.ai_mentor_be.dto.AiRoadmapSuggestionRequest;
import com.aimentor.ai_mentor_be.entity.Roadmap;
import com.aimentor.ai_mentor_be.repository.RoadmapRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiRoadmapService {

    private final GeminiService geminiService;
    private final RoadmapRepository roadmapRepository;
    private final ObjectMapper objectMapper;

    public List<AiRoadmapStageSuggestion> suggestStages(
            UUID userId,
            Long roadmapId,
            AiRoadmapSuggestionRequest request
    ) {

        // 1. Tìm roadmap
        Roadmap roadmap =
                roadmapRepository.findById(roadmapId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Roadmap not found"
                                )
                        );

        // 2. Kiểm tra quyền sở hữu
        if (!roadmap.getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        // 3. Validate request
        validateRequest(request);

        // 4. Tạo prompt
        String prompt = buildPrompt(request);

        // 5. Gọi Gemini
        String aiResult =
                geminiService.chat(prompt);

        // 6. Parse JSON
        try {

            String cleanJson =
                    cleanGeminiResponse(aiResult);

            List<AiRoadmapStageSuggestion> stages =
                    objectMapper.readValue(
                            cleanJson,
                            new TypeReference<
                                    List<AiRoadmapStageSuggestion>
                                    >() {}
                    );

            // 7. Validate kết quả AI
            validateAiStages(
                    stages,
                    request.getStartDate(),
                    request.getEndDate()
            );

            return stages;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Gemini response invalid: "
                            + e.getMessage()
            );
        }
    }

    private void validateRequest(
            AiRoadmapSuggestionRequest request
    ) {

        if (request.getRoadmapTitle() == null
                || request.getRoadmapTitle().isBlank()) {

            throw new RuntimeException(
                    "Roadmap title is required"
            );
        }

        if (request.getTopic() == null
                || request.getTopic().isBlank()) {

            throw new RuntimeException(
                    "Topic is required"
            );
        }

        if (request.getStartDate() == null
                || request.getEndDate() == null) {

            throw new RuntimeException(
                    "Start date and end date are required"
            );
        }

        if (request.getStartDate()
                .isAfter(request.getEndDate())) {

            throw new RuntimeException(
                    "Start date must be before end date"
            );
        }
    }

    private String buildPrompt(
            AiRoadmapSuggestionRequest request
    ) {

        return """
                Bạn là một AI Mentor chuyên xây dựng lộ trình học tập.

                Hãy xây dựng các GIAI ĐOẠN học tập hợp lý
                cho roadmap dưới đây.

                Tên roadmap:
                %s

                Chủ đề:
                %s

                Ngày bắt đầu:
                %s

                Ngày kết thúc:
                %s

                YÊU CẦU:

                1. Chỉ tạo GIAI ĐOẠN.
                2. KHÔNG tạo milestone.
                3. Không tạo task chi tiết.
                4. Chia lộ trình thành khoảng 4 đến 8 giai đoạn
                   tùy theo thời lượng thực tế.
                5. Các giai đoạn phải đi theo trình tự học tập hợp lý.
                6. Giai đoạn sau nên dựa trên kiến thức của giai đoạn trước.
                7. Không để các giai đoạn chồng chéo thời gian.
                8. Tất cả ngày phải nằm trong khoảng thời gian roadmap.
                9. Giai đoạn đầu phải bắt đầu từ hoặc gần ngày bắt đầu roadmap.
                10. Giai đoạn cuối phải kết thúc vào hoặc gần ngày kết thúc roadmap.
                11. Mỗi giai đoạn phải có mục tiêu rõ ràng.
                12. Không tạo nội dung ngoài JSON.

                Trả về DUY NHẤT JSON ARRAY theo format:

                [
                  {
                    "stageName": "Tên giai đoạn",
                    "stageGoal": "Mục tiêu của giai đoạn",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD"
                  }
                ]

                Không markdown.
                Không dùng ```json.
                Không giải thích.
                Không thêm text ngoài JSON.
                """
                .formatted(
                        request.getRoadmapTitle(),
                        request.getTopic(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getDescription()
                );
    }

    private String cleanGeminiResponse(
            String response
    ) {

        if (response == null) {

            throw new RuntimeException(
                    "Empty Gemini response"
            );
        }

        String result =
                response.trim();

        // Gemini đôi khi vẫn trả markdown
        if (result.startsWith("```json")) {

            result = result
                    .replaceFirst(
                            "^```json",
                            ""
                    )
                    .trim();

        } else if (result.startsWith("```")) {

            result = result
                    .replaceFirst(
                            "^```",
                            ""
                    )
                    .trim();
        }

        if (result.endsWith("```")) {

            result =
                    result.substring(
                            0,
                            result.length() - 3
                    ).trim();
        }

        return result;
    }

    private void validateAiStages(
            List<AiRoadmapStageSuggestion> stages,
            LocalDate roadmapStart,
            LocalDate roadmapEnd
    ) {

        if (stages == null || stages.isEmpty()) {

            throw new RuntimeException(
                    "AI did not generate any stages"
            );
        }

        if (stages.size() > 8) {

            throw new RuntimeException(
                    "AI generated too many stages"
            );
        }

        for (AiRoadmapStageSuggestion stage : stages) {

            if (stage.getStageName() == null
                    || stage.getStageName().isBlank()) {

                throw new RuntimeException(
                        "AI generated stage without name"
                );
            }

            if (stage.getStartDate() == null
                    || stage.getEndDate() == null) {

                throw new RuntimeException(
                        "AI generated stage without dates"
                );
            }

            if (stage.getStartDate()
                    .isAfter(stage.getEndDate())) {

                throw new RuntimeException(
                        "AI generated invalid stage dates"
                );
            }

            // Stage phải nằm trong roadmap
            if (stage.getStartDate()
                    .isBefore(roadmapStart)
                    ||
                    stage.getEndDate()
                            .isAfter(roadmapEnd)) {

                throw new RuntimeException(
                        "AI generated stage outside roadmap dates"
                );
            }
        }

        // Kiểm tra các giai đoạn có chồng nhau không
        for (int i = 0; i < stages.size(); i++) {

            for (int j = i + 1;
                 j < stages.size();
                 j++) {

                AiRoadmapStageSuggestion first =
                        stages.get(i);

                AiRoadmapStageSuggestion second =
                        stages.get(j);

                boolean overlap =
                        !first.getEndDate()
                                .isBefore(
                                        second.getStartDate()
                                )
                                &&
                                !second.getEndDate()
                                        .isBefore(
                                                first.getStartDate()
                                        );

                if (overlap) {

                    throw new RuntimeException(
                            "AI generated overlapping stages"
                    );
                }
            }
        }
    }
}