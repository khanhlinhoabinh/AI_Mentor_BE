package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.MoodFeedbackRequest;
import com.aimentor.ai_mentor_be.dto.NotificationResponse;
import com.aimentor.ai_mentor_be.entity.Notification;
import com.aimentor.ai_mentor_be.entity.NotificationStage;
import com.aimentor.ai_mentor_be.entity.NotificationType;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final GeminiService geminiService;

    /**
     * Lấy User hiện tại
     */
    private User getCurrentUser(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new RuntimeException("User not authenticated");
        }

        return user;
    }

    /**
     * Scheduler gọi hàm này để tạo Notification
     */
    public void createNotification(
            User user,
            String title,
            String content,
            NotificationType type,
            UUID referenceId,
            String referenceType,
            NotificationStage stage
    ) {

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .stage(stage)
                .isRead(false)
                .createdAt(Timestamp.from(Instant.now()))
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Lấy tất cả Notification
     */
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {

        User user = getCurrentUser(authentication);

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Chỉ lấy Notification chưa đọc
     */
    public List<NotificationResponse> getUnreadNotifications(Authentication authentication) {

        User user = getCurrentUser(authentication);

        return notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Đếm Notification chưa đọc
     */
    public long countUnread(Authentication authentication) {

        User user = getCurrentUser(authentication);

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Đánh dấu đã đọc
     */
    public void markAsRead(
            UUID notificationId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    /**
     * Đánh dấu đọc tất cả
     */
    public void markAllAsRead(Authentication authentication) {

        User user = getCurrentUser(authentication);

        List<Notification> notifications =
                notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    /**
     * Người dùng phản hồi 😊 😐 ☹️
     */
    public String feedbackToAI(
            UUID notificationId,
            MoodFeedbackRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        String mood = request.getMood().toUpperCase();

        String prompt = switch (mood) {

            case "HAPPY" -> """
                    Bạn là AI Mentor.

                    Người dùng vừa phản hồi rằng họ đã HOÀN THÀNH mục tiêu.

                    Tiêu đề Reminder:
                    %s

                    Nội dung Reminder:
                    %s

                    Hãy chúc mừng người dùng bằng khoảng 30-40 từ.

                    Giọng văn tích cực, thân thiện.

                    Không đánh số.
                    """.formatted(
                    notification.getTitle(),
                    notification.getContent()
            );

            case "NORMAL" -> """
                    Bạn là AI Mentor.

                    Người dùng mới hoàn thành MỘT PHẦN mục tiêu.

                    Tiêu đề Reminder:
                    %s

                    Nội dung Reminder:
                    %s

                    Hãy động viên người dùng tiếp tục.

                    Khoảng 30-40 từ.

                    Không đánh số.
                    """.formatted(
                    notification.getTitle(),
                    notification.getContent()
            );

            case "SAD" -> """
                    Bạn là AI Mentor.

                    Người dùng CHƯA hoàn thành mục tiêu.

                    Tiêu đề Reminder:
                    %s

                    Nội dung Reminder:
                    %s

                    Hãy động viên người dùng.

                    Không trách móc.

                    Khoảng 30-40 từ.

                    Không đánh số.
                    """.formatted(
                    notification.getTitle(),
                    notification.getContent()
            );

            default -> """
                    Bạn là AI Mentor.

                    Hãy động viên người dùng học tập bằng khoảng 30 từ.
                    """;
        };

        try {

            return geminiService.chat(prompt);

        } catch (Exception e) {

            return switch (mood) {

                case "HAPPY" ->
                        "🎉 Tuyệt vời! Bạn đã hoàn thành mục tiêu của mình. Hãy tiếp tục duy trì tinh thần học tập nhé!";

                case "NORMAL" ->
                        "💪 Bạn đã có một khởi đầu tốt. Hãy cố gắng hoàn thành phần còn lại nhé!";

                default ->
                        "😊 Không sao cả. Hôm nay là một cơ hội mới để tiếp tục. Cố lên!";
            };
        }
    }

    /**
     * Mapping Entity -> Response
     */
    private NotificationResponse mapToResponse(Notification notification) {

        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}