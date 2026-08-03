    package com.aimentor.ai_mentor_be.service;

    import com.aimentor.ai_mentor_be.entity.NotificationStage;
    import com.aimentor.ai_mentor_be.entity.NotificationType;
    import com.aimentor.ai_mentor_be.entity.Reminder;
    import com.aimentor.ai_mentor_be.entity.ReminderStatus;
    import com.aimentor.ai_mentor_be.repository.NotificationRepository;
    import com.aimentor.ai_mentor_be.repository.ReminderRepository;
    import com.aimentor.ai_mentor_be.entity.RoadmapMilestone;
    import com.aimentor.ai_mentor_be.entity.RoadmapStatus;
    import com.aimentor.ai_mentor_be.entity.User;
    import com.aimentor.ai_mentor_be.repository.RoadmapMilestoneRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Component;
    import java.time.temporal.ChronoUnit;

    import java.time.LocalDate;
    import java.util.List;

    @Component
    @RequiredArgsConstructor
    public class ReminderSchedulerService {

        private final ReminderRepository reminderRepository;
        private final NotificationRepository notificationRepository;
        private final NotificationService notificationService;
        private final GeminiService geminiService;
        private final RoadmapMilestoneRepository milestoneRepository;

        /**
         * Test: chạy mỗi 10 giây
         * Khi deploy đổi lại:
         * @Scheduled(cron = "0 0 8 * * *")
         */
        @Scheduled(fixedRate = 10000)
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
                                    reminder.getReminderId().toString(),
                                    NotificationStage.BEFORE_REMINDER
                            );

                    if (!exists) {

                        System.out.println("Create BEFORE_REMINDER");

                        notificationService.createNotification(
                                reminder.getUser(),
                                "📅 Nhắc lịch",
                                "Ngày mai bạn có lịch: " + reminder.getTitle(),
                                NotificationType.REMINDER,
                                reminder.getReminderId().toString(),
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
                                    reminder.getReminderId().toString(),
                                    NotificationStage.ON_REMINDER
                            );

                    if (!exists) {

                        System.out.println("Create ON_REMINDER");

                        notificationService.createNotification(
                                reminder.getUser(),
                                "⏰ Đến lịch",
                                "Hôm nay bạn cần thực hiện: " + reminder.getTitle(),
                                NotificationType.REMINDER,
                                reminder.getReminderId().toString(),
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
                                    reminder.getReminderId().toString(),
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
                                reminder.getReminderId().toString(),
                                "REMINDER",
                                NotificationStage.AFTER_REMINDER
                        );
                    }
                }
            }
            checkMilestoneNotifications(today);
        }
        private void checkMilestoneNotifications(LocalDate today) {
            System.out.println("===== CHECK MILESTONE =====");
            List<RoadmapMilestone> milestones =
                    milestoneRepository.findByStatusNot(
                            RoadmapStatus.COMPLETED
                    );
            System.out.println("Milestones = " + milestones.size());
            for (RoadmapMilestone milestone : milestones) {
                System.out.println("--------------------------------");
                System.out.println("Milestone ID = " + milestone.getMilestoneId());
                System.out.println("Title = " + milestone.getMilestoneTitle());
                System.out.println("Due = " + milestone.getDueDate());
                System.out.println("Status = " + milestone.getStatus());
                if (milestone.getStatus() == RoadmapStatus.COMPLETED) {
                    continue;
                }
                LocalDate dueDate = milestone.getDueDate();
                if (dueDate == null) {
                    continue;
                }
                long overdueDays =
                        ChronoUnit.DAYS.between(
                                dueDate,
                                today
                        );
                System.out.println("Today = " + today);
                System.out.println("Overdue Days = " + overdueDays);
                User user =
                        milestone.getRoadmapTask()
                                .getRoadmap()
                                .getUser();
                System.out.println("Today = " + today);
                System.out.println("Need = " + dueDate.plusDays(1));
                if (overdueDays == 1) {
                    System.out.println(">>> OVERDUE 1 DAY");
                    boolean exists =
                            notificationRepository.existsByReferenceIdAndStage(
                                    milestone.getMilestoneId().toString(),
                                    NotificationStage.MILESTONE_OVERDUE_1
                            );
                    System.out.println("Exists = " + exists);
                    if (!exists) {

                        notificationService.createNotification(
                                user,
                                "📌 Nhắc nhở milestone",
                                "Milestone \"" + milestone.getMilestoneTitle()
                                        + "\" đã quá hạn 1 ngày. Đừng quên cập nhật trạng thái hoàn thành.",
                                NotificationType.REMINDER,
                                milestone.getMilestoneId().toString(),
                                "MILESTONE",
                                NotificationStage.MILESTONE_OVERDUE_1
                        );
                    }
                }
                if (overdueDays == 2) {
                    boolean exists =
                            notificationRepository.existsByReferenceIdAndStage(
                                    milestone.getMilestoneId().toString(),
                                    NotificationStage.MILESTONE_OVERDUE_2
                            );
                    if (!exists) {
                        notificationService.createNotification(
                                user,
                                "⚠️ Milestone chậm tiến độ",
                                "Milestone \"" + milestone.getMilestoneTitle()
                                        + "\" đã chậm 2 ngày. Hãy hoàn thành sớm để không ảnh hưởng đến roadmap.",
                                NotificationType.REMINDER,
                                milestone.getMilestoneId().toString(),
                                "MILESTONE",
                                NotificationStage.MILESTONE_OVERDUE_2
                        );
                    }
                }
            }
        }
    }