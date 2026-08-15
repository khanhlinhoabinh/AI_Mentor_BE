package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.LearningEvaluationResponse;
import com.aimentor.ai_mentor_be.service.LearningEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning-evaluation")
@RequiredArgsConstructor
public class LearningEvaluationController {

    private final LearningEvaluationService learningEvaluationService;

    /**
     * Đánh giá hoạt động học tập trong N ngày gần nhất.
     * GET /api/learning-evaluation/weekly?days=7
     */
    @GetMapping("/weekly")
    public ResponseEntity<LearningEvaluationResponse> getWeeklyEvaluation(
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication
    ) {
        if (days < 1 || days > 31) {
            throw new IllegalArgumentException("days must be between 1 and 31");
        }

        return ResponseEntity.ok(
                learningEvaluationService.evaluate(authentication, days)
        );
    }
}