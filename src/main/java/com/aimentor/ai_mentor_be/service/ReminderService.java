package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.CreateReminderRequest;
import com.aimentor.ai_mentor_be.dto.ReminderResponse;
import com.aimentor.ai_mentor_be.dto.UpdateReminderRequest;
import com.aimentor.ai_mentor_be.entity.Reminder;
import com.aimentor.ai_mentor_be.entity.ReminderStatus;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;

    /**
     * Lấy User đang đăng nhập từ Spring Security
     */
    private User getCurrentUser(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new RuntimeException("User not authenticated");
        }

        return user;
    }

    /**
     * Tạo Reminder
     */
    public ReminderResponse createReminder(CreateReminderRequest request,
                                           Authentication authentication) {

        User user = getCurrentUser(authentication);

        Reminder reminder = Reminder.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .reminderDate(request.getReminderDate())
                .status(ReminderStatus.ACTIVE)
                .createdAt(Timestamp.from(Instant.now()))
                .build();

        reminderRepository.save(reminder);

        return mapToResponse(reminder);
    }

    /**
     * Lấy tất cả Reminder của user hiện tại
     */
    public List<ReminderResponse> getMyReminders(Authentication authentication) {

        User user = getCurrentUser(authentication);

        return reminderRepository.findByUserOrderByReminderDateAsc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lấy chi tiết Reminder
     */
    public ReminderResponse getReminder(UUID reminderId,
                                        Authentication authentication) {

        User user = getCurrentUser(authentication);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        if (!reminder.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(reminder);
    }

    /**
     * Cập nhật Reminder
     */
    public ReminderResponse updateReminder(UUID reminderId,
                                           UpdateReminderRequest request,
                                           Authentication authentication) {

        User user = getCurrentUser(authentication);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        if (!reminder.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setStatus(request.getStatus());

        reminderRepository.save(reminder);

        return mapToResponse(reminder);
    }

    /**
     * Xóa Reminder
     */
    public void deleteReminder(UUID reminderId,
                               Authentication authentication) {

        User user = getCurrentUser(authentication);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        if (!reminder.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        reminderRepository.delete(reminder);
    }

    /**
     * Mapping Entity -> DTO
     */
    private ReminderResponse mapToResponse(Reminder reminder) {

        return ReminderResponse.builder()
                .reminderId(reminder.getReminderId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .reminderDate(reminder.getReminderDate())
                .status(reminder.getStatus())
                .createdAt(reminder.getCreatedAt())
                .build();
    }
}