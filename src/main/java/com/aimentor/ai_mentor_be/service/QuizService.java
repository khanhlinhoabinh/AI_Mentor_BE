package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.*;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizSetRepository      quizSetRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository  quizAttemptRepository;
    private final SubjectRepository      subjectRepository;
    private final UserRepository         userRepository;
    private final GeminiService          geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ═══════════════════════════════════
    // 1. TẠO BỘ QUIZ
    // ═══════════════════════════════════
    public QuizSetResponse createQuizSet(User currentUser, CreateQuizSetRequest req) {

        Subject subject = null;
        if (req.getSubjectId() != null) {
            subject = subjectRepository.findById(req.getSubjectId()).orElse(null);
        }

        QuizSet quizSet = QuizSet.builder()
                .user(currentUser)
                .subject(subject)
                .title(req.getTitle())
                .questionType(req.getQuestionType())
                .difficulty(req.getDifficulty())
                .questionCount(req.getQuestionCount())
                .timeLimitSeconds(req.getTimeLimitSeconds())
                .pointsPerQuestion(req.getPointsPerQuestion())
                .shuffle(req.getShuffle() != null ? req.getShuffle() : false)
                .build();

        return mapToResponse(quizSetRepository.save(quizSet), currentUser);
    }

    // ═══════════════════════════════════
    // 2. LẤY DANH SÁCH BỘ QUIZ
    // ═══════════════════════════════════
    public List<QuizSetResponse> getMyQuizSets(User currentUser, Long subjectId) {
        List<QuizSet> sets = subjectId != null
                ? quizSetRepository.findByUserAndSubjectSubjectIdOrderByCreatedAtDesc(currentUser, subjectId)
                : quizSetRepository.findByUserOrderByCreatedAtDesc(currentUser);

        return sets.stream()
                .map(s -> mapToResponse(s, currentUser))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════
    // 3. XÓA BỘ QUIZ
    // ═══════════════════════════════════
    @Transactional
    public void deleteQuizSet(User currentUser, Long quizSetId) {
        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);

        // ✅ Xóa theo đúng thứ tự: attempts → questions → quiz_set
        quizAttemptRepository.deleteByQuizSet(quizSet);
        quizQuestionRepository.deleteByQuizSet(quizSet);
        quizSetRepository.delete(quizSet);
    }

    // ═══════════════════════════════════
    // 4. GEN CÂU HỎI BẰNG AI (từ text)
    // ═══════════════════════════════════
    public String generateQuestionsFromText(User currentUser, Long quizSetId, String sourceText) {
        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);
        String prompt   = buildPrompt(quizSet, sourceText);
        return geminiService.chat(prompt);
    }

    // ═══════════════════════════════════
    // 5. GEN CÂU HỎI BẰNG AI (từ file)
    // ═══════════════════════════════════
    public String generateQuestionsFromFile(
            User currentUser, Long quizSetId, MultipartFile file
    ) throws IOException {
        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);
        String text     = extractText(file);
        String prompt   = buildPrompt(quizSet, text);
        return geminiService.chat(prompt);
    }

    // ═══════════════════════════════════
    // 6. LƯU CÂU HỎI (sau khi review)
    // ═══════════════════════════════════
    @Transactional
    public List<QuizQuestionDTO> saveQuestions(
            User currentUser, Long quizSetId, SaveQuestionsRequest req
    ) {
        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);

        // Xóa câu hỏi cũ
        quizQuestionRepository.deleteByQuizSet(quizSet);

        // Lưu câu hỏi mới
        List<QuizQuestion> saved = new ArrayList<>();
        for (int i = 0; i < req.getQuestions().size(); i++) {
            QuizQuestionDTO dto = req.getQuestions().get(i);
            QuizQuestion q = QuizQuestion.builder()
                    .quizSet(quizSet)
                    .question(dto.getQuestion())
                    .optionsJson(dto.getOptionsJson())
                    .correctAnswer(dto.getCorrectAnswer())
                    .explanation(dto.getExplanation())
                    .orderIndex(i)
                    .build();
            saved.add(quizQuestionRepository.save(q));
        }

        return saved.stream().map(this::mapQuestion).collect(Collectors.toList());
    }

    // ═══════════════════════════════════
    // 7. LẤY CÂU HỎI (để làm bài)
    // ═══════════════════════════════════
    public List<QuizQuestionDTO> getQuestions(User currentUser, Long quizSetId) {
        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);
        List<QuizQuestion> questions =
                quizQuestionRepository.findByQuizSetOrderByOrderIndex(quizSet);

        // Xáo trộn nếu bật
        if (Boolean.TRUE.equals(quizSet.getShuffle())) {
            Collections.shuffle(questions);
        }

        // Khi lấy để LÀM BÀI: ẩn correctAnswer
        return questions.stream().map(q -> QuizQuestionDTO.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .optionsJson(q.getOptionsJson())
                .correctAnswer(null)   // ẩn đáp án
                .explanation(null)     // ẩn giải thích
                .orderIndex(q.getOrderIndex())
                .build()
        ).collect(Collectors.toList());
    }

    // ═══════════════════════════════════
    // 8. LẤY CÂU HỎI ĐẦY ĐỦ (để review/edit)
    // ═══════════════════════════════════
    public List<QuizQuestionDTO> getQuestionsForReview(User currentUser, Long quizSetId) {
        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);
        return quizQuestionRepository.findByQuizSetOrderByOrderIndex(quizSet)
                .stream().map(this::mapQuestion).collect(Collectors.toList());
    }

    // ═══════════════════════════════════
    // 9. NỘP BÀI & TÍNH ĐIỂM
    // ═══════════════════════════════════
    @Transactional
    public QuizResultResponse submitQuiz(
            User currentUser,
            Long quizSetId,
            SubmitQuizRequest req
    ) {

        QuizSet quizSet = getQuizSetOwned(currentUser, quizSetId);

        List<QuizQuestion> questions =
                quizQuestionRepository.findByQuizSetOrderByOrderIndex(quizSet);

        int correct = 0;

        for (QuizQuestion q : questions) {

            // ==========================
            // MULTIPLE CHOICE
            // ==========================
            if ("MULTIPLE_CHOICE".equals(quizSet.getQuestionType())) {

                String userAnswer =
                        req.getAnswers().get(String.valueOf(q.getId()));

                if (userAnswer != null
                        && userAnswer.trim().equalsIgnoreCase(
                        q.getCorrectAnswer() == null
                                ? ""
                                : q.getCorrectAnswer().trim())) {

                    correct++;
                }

            }

            // ==========================
            // TRUE FALSE
            // ==========================
            else {

                try {

                    List<Map<String, Object>> statements =
                            objectMapper.readValue(
                                    q.getOptionsJson(),
                                    new TypeReference<List<Map<String, Object>>>() {
                                    });

                    boolean allCorrect = true;

                    for (int i = 0; i < statements.size(); i++) {

                        String key = q.getId() + "_" + i;

                        String user =
                                req.getAnswers().get(key);

                        Boolean answer =
                                (Boolean) statements.get(i).get("answer");

                        if (user == null ||
                                !String.valueOf(answer)
                                        .equalsIgnoreCase(user)) {

                            allCorrect = false;
                            break;
                        }

                    }

                    if (allCorrect) {
                        correct++;
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(
                            "Cannot parse TRUE_FALSE question", ex);
                }

            }

        }

        double score =
                correct * quizSet.getPointsPerQuestion();

        double pct =
                questions.isEmpty()
                        ? 0
                        : (double) correct
                        / questions.size()
                        * 100;

        quizAttemptRepository
                .findTopByQuizSetAndUserOrderByAttemptedAtDesc(
                        quizSet,
                        currentUser)
                .ifPresent(quizAttemptRepository::delete);

        QuizAttempt attempt =
                QuizAttempt.builder()
                        .quizSet(quizSet)
                        .user(currentUser)
                        .score(score)
                        .totalQuestions(questions.size())
                        .correctCount(correct)
                        .answersJson(toJson(req.getAnswers()))
                        .build();

        quizAttemptRepository.save(attempt);

        return QuizResultResponse.builder()
                .score(score)
                .totalQuestions(questions.size())
                .correctCount(correct)
                .percentage(pct)
                .questions(
                        questions.stream()
                                .map(this::mapQuestion)
                                .collect(Collectors.toList()))
                .answersJson(toJson(req.getAnswers()))
                .build();

    }

    // ═══════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════

    private QuizSet getQuizSetOwned(User user, Long quizSetId) {
        QuizSet q = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new RuntimeException("Quiz set not found"));
        if (!q.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("You do not have permission");
        return q;
    }

    private String buildPrompt(QuizSet quizSet, String content) {
        String diffLabel = switch (quizSet.getDifficulty()) {
            case "EASY"   -> "Dễ";
            case "MEDIUM" -> "Trung Bình";
            case "HARD"   -> "Khó";
            default       -> quizSet.getDifficulty();
        };

        boolean isMultipleChoice = "MULTIPLE_CHOICE".equals(quizSet.getQuestionType());

        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là giáo viên nhiều năm kinh nghiệm.\n");
        prompt.append("Hãy tạo bộ câu hỏi từ tài liệu sau.\n\n");
        prompt.append("Yêu cầu:\n");
        prompt.append("- Cấp độ: ").append(diffLabel).append("\n");
        prompt.append("- Số lượng: ").append(quizSet.getQuestionCount()).append(" câu\n");

        if (isMultipleChoice) {
            prompt.append("- Loại câu hỏi: Trắc nghiệm chọn 1 đáp án đúng\n");
            prompt.append("- Chỉ sử dụng kiến thức trong tài liệu.\n");
            prompt.append("- Không tự bịa kiến thức.\n");
            prompt.append("- Mỗi câu có đúng 4 đáp án.\n");
            prompt.append("- Chỉ có đúng 1 đáp án đúng.\n");
            prompt.append("- Các đáp án sai phải hợp lý.\n");
            prompt.append("- Không lặp câu hỏi.\n");
            prompt.append("- Phân bố đều các chương.\n");
            prompt.append("- Trả về JSON duy nhất, không có text thừa, không có markdown.\n\n");
            prompt.append("Format JSON:\n");
            prompt.append("""
[
  {
    "question": "...",
    "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
    "correctAnswer": "A",
    "explanation": "..."
  }
]
""");
        } else {
            prompt.append("- Loại câu hỏi: Câu hỏi đúng/sai\n");
            prompt.append("- Mỗi câu gồm 4 nhận định.\n");
            prompt.append("- Mỗi nhận định có true hoặc false.\n");
            prompt.append("- Không lặp câu hỏi.\n");
            prompt.append("- Trả về JSON duy nhất, không có text thừa, không có markdown.\n\n");
            prompt.append("Format JSON:\n");
            prompt.append("""
[
  {
    "question": "...",
    "statements": [
      {"content": "...", "answer": true},
      {"content": "...", "answer": false},
      {"content": "...", "answer": true},
      {"content": "...", "answer": false}
    ],
    "explanation": "..."
  }
]
""");
        }

        prompt.append("\n=== TÀI LIỆU ===\n");
        // Giới hạn 8000 ký tự
        String limited = content.length() > 8000
                ? content.substring(0, 8000) + "\n...(nội dung bị cắt)"
                : content;
        prompt.append(limited);

        return prompt.toString();
    }

    private String extractText(MultipartFile file) throws IOException {
        String name = (file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "").toLowerCase();
        if (name.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(doc);
            }
        }
        return new String(file.getBytes());
    }

    private QuizQuestionDTO mapQuestion(QuizQuestion q) {
        return QuizQuestionDTO.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .optionsJson(q.getOptionsJson())
                .correctAnswer(q.getCorrectAnswer())
                .explanation(q.getExplanation())
                .orderIndex(q.getOrderIndex())
                .build();
    }

    private QuizSetResponse mapToResponse(QuizSet s, User user) {
        int actualCount = quizQuestionRepository
                .findByQuizSetOrderByOrderIndex(s).size();

        QuizAttemptResponse lastAttempt = quizAttemptRepository
                .findTopByQuizSetAndUserOrderByAttemptedAtDesc(s, user)
                .map(a -> QuizAttemptResponse.builder()
                        .id(a.getId())
                        .score(a.getScore())
                        .totalQuestions(a.getTotalQuestions())
                        .correctCount(a.getCorrectCount())
                        .answersJson(a.getAnswersJson())
                        .attemptedAt(a.getAttemptedAt())
                        .build())
                .orElse(null);

        return QuizSetResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .subjectId(s.getSubject() != null ? s.getSubject().getSubjectId() : null)
                .subjectName(s.getSubject() != null ? s.getSubject().getSubjectName() : null)
                .questionType(s.getQuestionType())
                .difficulty(s.getDifficulty())
                .questionCount(s.getQuestionCount())
                .timeLimitSeconds(s.getTimeLimitSeconds())
                .pointsPerQuestion(s.getPointsPerQuestion())
                .shuffle(s.getShuffle())
                .createdAt(s.getCreatedAt())
                .actualQuestionCount(actualCount)
                .lastAttempt(lastAttempt)
                .build();
    }

    private String toJson(Map<String, String> map) {

        StringBuilder sb = new StringBuilder("{");

        map.forEach((k, v) -> sb
                .append("\"")
                .append(k)
                .append("\":\"")
                .append(v)
                .append("\","));

        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");

        return sb.toString();
    }
}