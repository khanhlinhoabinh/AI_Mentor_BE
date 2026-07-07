package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.entity.NotificationStage;
import com.aimentor.ai_mentor_be.entity.NotificationType;
import com.aimentor.ai_mentor_be.entity.Reminder;
import com.aimentor.ai_mentor_be.entity.ReminderStatus;
import com.aimentor.ai_mentor_be.repository.NotificationRepository;
import com.aimentor.ai_mentor_be.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderSchedulerService {

    private final ReminderRepository reminderRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final GeminiService geminiService;

    /**
     * Test: chạy mỗi 10 giây
     * Khi deploy đổi lại:
     * @Scheduled(cron = "0 0 8 * * *")
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkReminderNotifications() {

        System.out.println("===== Scheduler Running =====");

        LocalDate today = LocalDate.now();

        List<Reminder> reminders = reminderRepository.findAll();

        for (Reminder reminder : reminders) {

            if (reminder.getStatus() != ReminderStatus.ACTIVE) {
                continue;
            }

            LocalDate reminderDate = reminder.getReminderDate();

            System.out.println("--------------------------------");
            System.out.println("Reminder : " + reminder.getTitle());
            System.out.println("Reminder Date : " + reminderDate);
            System.out.println("Today : " + today);

            /*
             * ===========================================
             * 1. TRƯỚC 1 NGÀY
             * ===========================================
             */

            if (today.equals(reminderDate.minusDays(1))) {

                boolean exists =
                        notificationRepository.existsByReferenceIdAndStage(
                                reminder.getReminderId(),
                                NotificationStage.BEFORE_REMINDER
                        );

                if (!exists) {

                    System.out.println("Create BEFORE_REMINDER");

                    notificationService.createNotification(
                            reminder.getUser(),
                            "📅 Nhắc lịch",
                            "Ngày mai bạn có lịch: " + reminder.getTitle(),
                            NotificationType.REMINDER,
                            reminder.getReminderId(),
                            "REMINDER",
                            NotificationStage.BEFORE_REMINDER
                    );
                }
            }

            /*
             * ===========================================
             * 2. ĐÚNG NGÀY
             * ===========================================
             */

            if (today.equals(reminderDate)) {

                boolean exists =
                        notificationRepository.existsByReferenceIdAndStage(
                                reminder.getReminderId(),
                                NotificationStage.ON_REMINDER
                        );

                if (!exists) {

                    System.out.println("Create ON_REMINDER");

                    notificationService.createNotification(
                            reminder.getUser(),
                            "⏰ Đến lịch",
                            "Hôm nay bạn cần thực hiện: " + reminder.getTitle(),
                            NotificationType.REMINDER,
                            reminder.getReminderId(),
                            "REMINDER",
                            NotificationStage.ON_REMINDER
                    );
                }
            }

            /*
             * ===========================================
             * 3. SAU 1 NGÀY (AI HỎI THĂM)
             * ===========================================
             */

            if (today.equals(reminderDate.plusDays(1))) {

                boolean exists =
                        notificationRepository.existsByReferenceIdAndStage(
                                reminder.getReminderId(),
                                NotificationStage.AFTER_REMINDER
                        );

                if (!exists) {

                    System.out.println("Create AFTER_REMINDER");

                    String aiMessage;

                    try {

                        String prompt = """
                                Bạn là AI Mentor.

                                Người dùng đã có lịch:

                                %s

                                Hãy viết một tin nhắn khoảng 30-40 từ.

                                Yêu cầu:
                                - Hỏi người dùng hôm qua đã hoàn thành chưa.
                                - Nếu chưa thì động viên.
                                - Nếu hoàn thành thì chúc mừng.
                                - Văn phong thân thiện.
                                - Không đánh số.
                                - Chỉ trả về đúng nội dung.
                                """.formatted(reminder.getTitle());

                        aiMessage = geminiService.chat(prompt);

                    } catch (Exception e) {

                        aiMessage =
                                "Hôm qua bạn đã hoàn thành \"" +
                                        reminder.getTitle() +
                                        "\" chưa? Nếu chưa cũng không sao, hôm nay hãy tiếp tục nhé!";
                    }

                    notificationService.createNotification(
                            reminder.getUser(),
                            "🤖 AI Mentor",
                            aiMessage,
                            NotificationType.AI_MESSAGE,
                            reminder.getReminderId(),
                            "REMINDER",
                            NotificationStage.AFTER_REMINDER
                    );
                }
            }

        }

    }

}