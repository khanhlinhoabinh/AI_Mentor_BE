package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.flashcard.*;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;
import com.aimentor.ai_mentor_be.entity.enums.FlashcardType;
import com.aimentor.ai_mentor_be.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiFlashcardService {

    private final DocumentRepository documentRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final GeminiService geminiService;

    private final ObjectMapper objectMapper;
    public FlashcardSetFullResponse generate(
            User user,
            GenerateFlashcardRequest request
    ) {

        Document document =
                documentRepository.findById(
                        request.getDocumentId()
                ).orElseThrow(() ->
                        new RuntimeException("Document not found"));

        if (!document.getUploadedBy()
                .getUserId()
                .equals(user.getUserId())) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        String aiPrompt =
                buildPrompt(
                        request,
                        document.getExtractedText()
                );

        String aiResult =
                geminiService.generateFlashcards(
                        aiPrompt
                );

        List<AiFlashcardItem> cards;

        try {

            cards = objectMapper.readValue(
                    aiResult,
                    new TypeReference<List<AiFlashcardItem>>() {
                    }
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Gemini response invalid"
            );
        }

        FlashcardSet flashcardSet =
                FlashcardSet.builder()
                        .createdBy(user)
                        .subject(document.getSubject())
                        .setName(request.getSetName())
                        .description(request.getPrompt())
                        .sourceType(
                                FlashcardSourceType.AI
                        )
                        .totalCards(cards.size())
                        .build();

        flashcardSet =
                flashcardSetRepository.save(
                        flashcardSet
                );

        List<FlashcardResponse> responses =
                new ArrayList<>();

        int order = 1;

        for (AiFlashcardItem item : cards) {

            Flashcard flashcard =
                    Flashcard.builder()
                            .flashcardSet(flashcardSet)
                            .cardType(
                                    FlashcardType.QA
                            )
                            .frontContent(
                                    item.getFront()
                            )
                            .backContent(
                                    item.getBack()
                            )
                            .displayOrder(order++)
                            .build();

            flashcard =
                    flashcardRepository.save(
                            flashcard
                    );

            responses.add(
                    FlashcardResponse.builder()
                            .flashcardId(
                                    flashcard.getFlashcardId()
                            )
                            .flashcardSetId(
                                    flashcardSet.getFlashcardSetId()
                            )
                            .cardType(
                                    flashcard.getCardType()
                            )
                            .frontContent(
                                    flashcard.getFrontContent()
                            )
                            .backContent(
                                    flashcard.getBackContent()
                            )
                            .displayOrder(
                                    flashcard.getDisplayOrder()
                            )
                            .createdAt(
                                    flashcard.getCreatedAt()
                            )
                            .build()
            );
        }

        return FlashcardSetFullResponse.builder()
                .flashcardSetId(
                        flashcardSet.getFlashcardSetId()
                )
                .setName(
                        flashcardSet.getSetName()
                )
                .description(
                        flashcardSet.getDescription()
                )
                .sourceType(
                        flashcardSet.getSourceType()
                )
                .totalCards(
                        flashcardSet.getTotalCards()
                )
                .createdAt(
                        flashcardSet.getCreatedAt()
                )
                .cards(
                        responses
                )
                .build();
    }

    private String buildPrompt(
            GenerateFlashcardRequest request,
            String content
    ) {

        return """
                Bạn là trợ lý học tập.
                
                Từ tài liệu dưới đây hãy tạo chính xác %d flashcard.
                
                Trả về DUY NHẤT JSON.
                
                Format:
                
                [
                 {
                   "front":"...",
                   "back":"..."
                 }
                ]
                
                Không markdown.
                Không giải thích.
                Không text ngoài JSON.
                
                Yêu cầu:
                
                %s
                
                Tài liệu:
                
                %s
                """
                .formatted(
                        request.getNumberOfCards(),
                        request.getPrompt(),
                        content
                );
    }
}