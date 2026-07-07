package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Reminder;
import com.aimentor.ai_mentor_be.entity.ReminderStatus;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByUser(User user);

    List<Reminder> findByUserOrderByReminderDateAsc(User user);

    List<Reminder> findByReminderDate(LocalDate reminderDate);

    List<Reminder> findByReminderDateAndStatus(LocalDate reminderDate,
                                               ReminderStatus status);

}