package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.*;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // ── 1. Tạo bộ quiz
    @PostMapping("/sets")
    public ResponseEntity<QuizSetResponse> createQuizSet(
            @RequestBody CreateQuizSetRequest req) {
        return ResponseEntity.ok(
                quizService.createQuizSet(getCurrentUser(), req));
    }

    // ── 2. Lấy danh sách bộ quiz (toàn bộ hoặc theo môn)
    @GetMapping("/sets")
    public ResponseEntity<List<QuizSetResponse>> getMyQuizSets(
            @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(
                quizService.getMyQuizSets(getCurrentUser(), subjectId));
    }

    // ── 3. Xóa bộ quiz
    @DeleteMapping("/sets/{quizSetId}")
    public ResponseEntity<String> deleteQuizSet(
            @PathVariable Long quizSetId) {
        quizService.deleteQuizSet(getCurrentUser(), quizSetId);
        return ResponseEntity.ok("Quiz set deleted successfully");
    }

    // ── 4. Gen câu hỏi từ TEXT
    @PostMapping("/sets/{quizSetId}/generate/text")
    public ResponseEntity<String> generateFromText(
            @PathVariable Long quizSetId,
            @RequestBody GenerateQuizRequest req) {
        return ResponseEntity.ok(
                quizService.generateQuestionsFromText(
                        getCurrentUser(), quizSetId, req.getSourceText()));
    }

    // ── 5. Gen câu hỏi từ FILE (PDF)
    @PostMapping(value = "/sets/{quizSetId}/generate/file",
            consumes = "multipart/form-data")
    public ResponseEntity<String> generateFromFile(
            @PathVariable Long quizSetId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(
                quizService.generateQuestionsFromFile(
                        getCurrentUser(), quizSetId, file));
    }

    // ── 6. Lưu câu hỏi (sau khi review)
    @PostMapping("/sets/{quizSetId}/questions")
    public ResponseEntity<List<QuizQuestionDTO>> saveQuestions(
            @PathVariable Long quizSetId,
            @RequestBody SaveQuestionsRequest req) {
        return ResponseEntity.ok(
                quizService.saveQuestions(getCurrentUser(), quizSetId, req));
    }

    // ── 7. Lấy câu hỏi để làm bài (ẩn đáp án)
    @GetMapping("/sets/{quizSetId}/questions")
    public ResponseEntity<List<QuizQuestionDTO>> getQuestions(
            @PathVariable Long quizSetId) {
        return ResponseEntity.ok(
                quizService.getQuestions(getCurrentUser(), quizSetId));
    }

    // ── 8. Lấy câu hỏi để review/edit (hiện đáp án)
    @GetMapping("/sets/{quizSetId}/questions/review")
    public ResponseEntity<List<QuizQuestionDTO>> getQuestionsForReview(
            @PathVariable Long quizSetId) {
        return ResponseEntity.ok(
                quizService.getQuestionsForReview(getCurrentUser(), quizSetId));
    }

    // ── 9. Nộp bài & tính điểm
    @PostMapping("/sets/{quizSetId}/submit")
    public ResponseEntity<QuizResultResponse> submit(
            @PathVariable Long quizSetId,
            @RequestBody SubmitQuizRequest req) {
        return ResponseEntity.ok(
                quizService.submitQuiz(getCurrentUser(), quizSetId, req));
    }
}