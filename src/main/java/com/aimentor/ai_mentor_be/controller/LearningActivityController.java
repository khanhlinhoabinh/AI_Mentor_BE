package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.RecordFlashcardStudyRequest;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.LearningActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning-activity")
@RequiredArgsConstructor
public class LearningActivityController {

    private final LearningActivityService learningActivityService;

    /**
     * FE gọi khi người dùng hoàn thành một phiên ôn flashcard.
     * POST /api/learning-activity/flashcards/study
     */
    @PostMapping("/flashcards/study")
    public ResponseEntity<String> recordFlashcardStudy(
            @RequestBody RecordFlashcardStudyRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        learningActivityService.recordFlashcardStudy(user, request);

        return ResponseEntity.ok("Flashcard study activity recorded");
    }
}