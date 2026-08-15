package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.LearningEvaluationResponse;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningEvaluationService {

    private static final Set<String> LEARNING_ACTIONS = Set.of(
            "SUBMIT_QUIZ",
            "STUDY_FLASHCARD"
    );

    private final ActivityLogRepository activityLogRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private final RoadmapMilestoneRepository roadmapMilestoneRepository;
    private final UserStreakRepository userStreakRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public LearningEvaluationResponse evaluate(
            Authentication authentication,
            int days
    ) {
        User user = (User) authentication.getPrincipal();

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1);

        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = LocalDateTime.now();

        Timestamp from = Timestamp.valueOf(fromDateTime);
        Timestamp to = Timestamp.valueOf(toDateTime);

        List<ActivityLog> activities =
                activityLogRepository
                        .findByUserAndCreatedAtBetweenOrderByCreatedAtAsc(user, from, to);

        List<QuizAttempt> attempts =
                quizAttemptRepository
                        .findByUserAndAttemptedAtBetweenOrderByAttemptedAtAsc(user, from, to);

        RoadmapStats roadmapStats = collectRoadmapStats(user, today);
        StreakStats streakStats = collectStreakStats(user);

        LearningEvaluationResponse.WeeklyMetrics metrics =
                buildMetrics(activities, attempts, roadmapStats, streakStats);

        int activityScore = calculateActivityScore(metrics, days);
        String status = determineStatus(metrics, activityScore);

        AiEvaluation ai = generateAiEvaluation(
                user, fromDate, today, days, metrics, activityScore, status
        );

        return LearningEvaluationResponse.builder()
                .periodLabel(days == 7 ? "7 ngày gần nhất" : days + " ngày gần nhất")
                .fromDate(fromDate)
                .toDate(today)
                .status(status)
                .activityScore(activityScore)
                .metrics(metrics)
                .aiSummary(ai.summary())
                .weeklyOverview(ai.weeklyOverview())
                .strengths(ai.strengths())
                .concerns(ai.concerns())
                .recommendations(ai.recommendations())
                .build();
    }

    private LearningEvaluationResponse.WeeklyMetrics buildMetrics(
            List<ActivityLog> activities,
            List<QuizAttempt> attempts,
            RoadmapStats roadmapStats,
            StreakStats streakStats
    ) {
        Set<LocalDate> activeDays = new HashSet<>();
        Set<LocalDate> loginDays = new HashSet<>();

        int flashcardSessions = 0;
        int flashcardsReviewed = 0;

        for (ActivityLog activity : activities) {
            if (activity.getCreatedAt() == null) continue;

            LocalDate date = activity.getCreatedAt().toLocalDateTime().toLocalDate();

            if ("LOGIN".equals(activity.getAction())) {
                loginDays.add(date);
            }

            if (LEARNING_ACTIONS.contains(activity.getAction())) {
                activeDays.add(date);
            }

            if ("SUBMIT_QUIZ".equals(activity.getAction())) {
                activeDays.add(date);
            }

            if ("STUDY_FLASHCARD".equals(activity.getAction())) {
                activeDays.add(date);
                flashcardSessions++;
                flashcardsReviewed += parseIntegerFromDescription(
                        activity.getDescription(), "cardsReviewed");
            }
        }

        for (QuizAttempt attempt : attempts) {
            if (attempt.getAttemptedAt() != null) {
                activeDays.add(attempt.getAttemptedAt().toLocalDateTime().toLocalDate());
            }
        }

        double averageQuiz = attempts.isEmpty() ? 0
                : attempts.stream().mapToDouble(this::percentage).average().orElse(0);

        double bestQuiz = attempts.isEmpty() ? 0
                : attempts.stream().mapToDouble(this::percentage).max().orElse(0);

        double latestQuiz = attempts.isEmpty() ? 0
                : percentage(attempts.get(attempts.size() - 1));

        return LearningEvaluationResponse.WeeklyMetrics.builder()
                .activeDays(activeDays.size())
                .loginDays(loginDays.size())
                .quizAttempts(attempts.size())
                .averageQuizPercentage(round(averageQuiz))
                .bestQuizPercentage(round(bestQuiz))
                .latestQuizPercentage(round(latestQuiz))
                .flashcardStudySessions(flashcardSessions)
                .flashcardsReviewed(flashcardsReviewed)
                .overdueMilestones(roadmapStats.overdueMilestones())
                .overdueTasks(roadmapStats.overdueTasks())
                .activeRoadmaps(roadmapStats.activeRoadmaps())
                .averageRoadmapProgress(round(roadmapStats.averageProgress()))
                .currentStreak(streakStats.currentStreak())
                .longestStreak(streakStats.longestStreak())
                .build();
    }

    /** Điểm hoạt động chỉ phản ánh mức độ tham gia học tập, không phải điểm số kiến thức. */
    private int calculateActivityScore(
            LearningEvaluationResponse.WeeklyMetrics m,
            int days
    ) {
        double activeDayScore = Math.min(40.0, m.getActiveDays() * 40.0 / days);
        double quizScore = Math.min(25.0, m.getQuizAttempts() * 8.0);
        double flashcardScore = Math.min(20.0, m.getFlashcardStudySessions() * 5.0);

        double roadmapScore =
                m.getOverdueMilestones() == 0 && m.getOverdueTasks() == 0
                        ? 15.0
                        : Math.max(0, 15.0
                        - (m.getOverdueMilestones() * 4.0)
                        - (m.getOverdueTasks() * 3.0));

        return (int) Math.round(
                Math.min(100, activeDayScore + quizScore + flashcardScore + roadmapScore)
        );
    }

    private String determineStatus(
            LearningEvaluationResponse.WeeklyMetrics m,
            int activityScore
    ) {
        if (m.getActiveDays() == 0
                && m.getQuizAttempts() == 0
                && m.getFlashcardStudySessions() == 0) {
            return "INACTIVE";
        }

        if (m.getOverdueMilestones() >= 2
                || m.getOverdueTasks() >= 2
                || activityScore < 35) {
            return "NEEDS_ATTENTION";
        }

        return "ON_TRACK";
    }

    private AiEvaluation generateAiEvaluation(
            User user,
            LocalDate fromDate,
            LocalDate toDate,
            int days,
            LearningEvaluationResponse.WeeklyMetrics metrics,
            int activityScore,
            String status
    ) {
        String prompt = """
                Bạn là AI Mentor của một hệ thống học tập cá nhân hóa.

                Hãy đánh giá hoạt động học tập của người dùng dựa CHỈ trên dữ liệu thống kê
                bên dưới. Không được bịa thêm hoạt động, điểm số hoặc nguyên nhân mà dữ liệu
                không chứng minh được.

                Người học:
                - Tên: %s

                Khoảng thời gian:
                - Từ: %s
                - Đến: %s
                - Số ngày: %d

                Chỉ số backend tính được:
                - activeDays: %d
                - loginDays: %d
                - quizAttempts: %d
                - averageQuizPercentage: %.1f
                - bestQuizPercentage: %.1f
                - latestQuizPercentage: %.1f
                - flashcardStudySessions: %d
                - flashcardsReviewed: %d
                - overdueMilestones: %d
                - overdueTasks: %d
                - activeRoadmaps: %d
                - averageRoadmapProgress: %.1f
                - currentStreak: %d
                - longestStreak: %d
                - backendActivityScore: %d
                - backendStatus: %s

                Yêu cầu:
                1. Tóm tắt tuần vừa rồi người học đã học như thế nào.
                2. Nêu rõ điểm tích cực nếu có.
                3. Nêu vấn đề cần chú ý nếu có.
                4. Đưa ra 2-4 gợi ý hành động cụ thể cho giai đoạn tiếp theo.
                5. Nếu dữ liệu chưa đủ để kết luận về một khía cạnh, phải nói rõ là
                   "chưa đủ dữ liệu", không được suy đoán.
                6. Không dùng giọng trách móc.
                7. Viết bằng tiếng Việt, ngắn gọn, thực tế.

                Trả về DUY NHẤT JSON, không markdown:
                {
                  "summary": "Một đoạn nhận xét tổng quan",
                  "weeklyOverview": "Tóm tắt hoạt động trong kỳ",
                  "strengths": ["...", "..."],
                  "concerns": ["...", "..."],
                  "recommendations": ["...", "..."]
                }
                """.formatted(
                user.getFullName(),
                fromDate, toDate, days,
                safe(metrics.getActiveDays()),
                safe(metrics.getLoginDays()),
                safe(metrics.getQuizAttempts()),
                safe(metrics.getAverageQuizPercentage()),
                safe(metrics.getBestQuizPercentage()),
                safe(metrics.getLatestQuizPercentage()),
                safe(metrics.getFlashcardStudySessions()),
                safe(metrics.getFlashcardsReviewed()),
                safe(metrics.getOverdueMilestones()),
                safe(metrics.getOverdueTasks()),
                safe(metrics.getActiveRoadmaps()),
                safe(metrics.getAverageRoadmapProgress()),
                safe(metrics.getCurrentStreak()),
                safe(metrics.getLongestStreak()),
                activityScore,
                status
        );

        try {
            String raw = geminiService.chat(prompt);
            String json = cleanJson(raw);

            Map<String, Object> result =
                    objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            return new AiEvaluation(
                    getString(result, "summary", fallbackSummary(metrics, status)),
                    getString(result, "weeklyOverview", fallbackOverview(metrics)),
                    getStringList(result, "strengths"),
                    getStringList(result, "concerns"),
                    getStringList(result, "recommendations")
            );
        } catch (Exception e) {
            return fallbackEvaluation(metrics, status);
        }
    }

    private AiEvaluation fallbackEvaluation(
            LearningEvaluationResponse.WeeklyMetrics m,
            String status
    ) {
        String summary = fallbackSummary(m, status);

        List<String> strengths = new ArrayList<>();
        List<String> concerns = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (m.getQuizAttempts() > 0) {
            strengths.add("Bạn đã có hoạt động làm quiz trong kỳ đánh giá.");
        }

        if (m.getFlashcardStudySessions() > 0) {
            strengths.add("Bạn đã duy trì việc ôn flashcard.");
        }

        if (m.getCurrentStreak() != null && m.getCurrentStreak() > 0) {
            strengths.add("Bạn đang có chuỗi học liên tiếp " + m.getCurrentStreak() + " ngày.");
        }

        if (m.getOverdueMilestones() > 0) {
            concerns.add("Có " + m.getOverdueMilestones() + " milestone đang quá hạn.");
        }

        if (m.getOverdueTasks() > 0) {
            concerns.add("Có " + m.getOverdueTasks() + " task đang quá hạn.");
        }

        if (m.getActiveDays() < 3) {
            concerns.add("Số ngày có hoạt động học tập còn khá ít trong kỳ.");
        }

        if (m.getQuizAttempts() == 0) {
            recommendations.add("Thử hoàn thành ít nhất một bài quiz để kiểm tra mức độ ghi nhớ.");
        }

        if (m.getFlashcardStudySessions() == 0) {
            recommendations.add("Thêm một phiên ôn flashcard ngắn để củng cố kiến thức.");
        }

        if (m.getOverdueMilestones() > 0 || m.getOverdueTasks() > 0) {
            recommendations.add("Ưu tiên xử lý các milestone/task đã quá hạn trước khi mở rộng mục tiêu mới.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Tiếp tục duy trì nhịp học hiện tại và kiểm tra lại tiến độ vào cuối tuần.");
        }

        return new AiEvaluation(
                summary,
                fallbackOverview(m),
                limit(strengths),
                limit(concerns),
                limit(recommendations)
        );
    }

    private String fallbackSummary(
            LearningEvaluationResponse.WeeklyMetrics m,
            String status
    ) {
        if ("INACTIVE".equals(status)) {
            return "Trong kỳ đánh giá chưa ghi nhận hoạt động học tập đáng kể. Hãy bắt đầu lại bằng một mục tiêu nhỏ và dễ hoàn thành.";
        }

        if ("NEEDS_ATTENTION".equals(status)) {
            return "Nhịp học đang cần được củng cố. Bạn vẫn có hoạt động học tập nhưng một số chỉ số như tiến độ hoặc lịch học đang cần được chú ý.";
        }

        return "Bạn đang duy trì hoạt động học tập tương đối ổn định. Hãy tiếp tục giữ nhịp và củng cố những phần kiến thức còn chưa chắc.";
    }

    private String fallbackOverview(LearningEvaluationResponse.WeeklyMetrics m) {
        return "Có " + m.getActiveDays() + " ngày có hoạt động học tập, "
                + m.getQuizAttempts() + " lần làm quiz và "
                + m.getFlashcardStudySessions() + " phiên ôn flashcard trong kỳ đánh giá.";
    }

    private RoadmapStats collectRoadmapStats(User user, LocalDate today) {
        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);

        int activeRoadmaps = 0;
        int overdueTasks = 0;
        int overdueMilestones = 0;
        double progressSum = 0;

        for (Roadmap roadmap : roadmaps) {
            if (roadmap.getStatus() != RoadmapStatus.COMPLETED) {
                activeRoadmaps++;
            }

            if (roadmap.getProgressPercent() != null) {
                progressSum += roadmap.getProgressPercent();
            }

            List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmap(roadmap);

            for (RoadmapTask task : tasks) {
                if (task.getStatus() != RoadmapTaskStatus.COMPLETED
                        && task.getEndDate() != null
                        && task.getEndDate().isBefore(today)) {
                    overdueTasks++;
                }

                List<RoadmapMilestone> milestones =
                        roadmapMilestoneRepository.findByRoadmapTask(task);

                for (RoadmapMilestone milestone : milestones) {
                    if (milestone.getStatus() != RoadmapStatus.COMPLETED
                            && milestone.getDueDate() != null
                            && milestone.getDueDate().isBefore(today)) {
                        overdueMilestones++;
                    }
                }
            }
        }

        double averageProgress = roadmaps.isEmpty() ? 0 : progressSum / roadmaps.size();

        return new RoadmapStats(activeRoadmaps, overdueTasks, overdueMilestones, averageProgress);
    }

    private StreakStats collectStreakStats(User user) {
        return userStreakRepository.findByUser(user)
                .map(s -> new StreakStats(
                        s.getCurrentStreak() == null ? 0 : s.getCurrentStreak(),
                        s.getLongestStreak() == null ? 0 : s.getLongestStreak()
                ))
                .orElse(new StreakStats(0, 0));
    }

    private double percentage(QuizAttempt attempt) {
        if (attempt.getTotalQuestions() == null || attempt.getTotalQuestions() == 0) {
            return 0;
        }
        return attempt.getCorrectCount() * 100.0 / attempt.getTotalQuestions();
    }

    private String cleanJson(String raw) {
        if (raw == null) {
            throw new RuntimeException("Empty AI response");
        }

        String cleaned = raw.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    private String getString(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null || value.toString().isBlank() ? fallback : value.toString();
    }

    private List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (!(value instanceof List<?> list)) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .limit(5)
                .collect(Collectors.toList());
    }

    private int parseIntegerFromDescription(String description, String field) {
        if (description == null) return 0;

        Pattern pattern = Pattern.compile(Pattern.quote(field) + "\\s*=\\s*(\\d+)");
        Matcher matcher = pattern.matcher(description);

        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private List<String> limit(List<String> list) {
        return list.stream().limit(5).collect(Collectors.toList());
    }

    private int safe(Integer value) { return value == null ? 0 : value; }
    private double safe(Double value) { return value == null ? 0 : value; }
    private double round(double value) { return Math.round(value * 10.0) / 10.0; }

    private record RoadmapStats(
            int activeRoadmaps, int overdueTasks, int overdueMilestones, double averageProgress
    ) {}

    private record StreakStats(int currentStreak, int longestStreak) {}

    private record AiEvaluation(
            String summary, String weeklyOverview,
            List<String> strengths, List<String> concerns, List<String> recommendations
    ) {}
}