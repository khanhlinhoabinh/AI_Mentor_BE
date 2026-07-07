package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Notification;
import com.aimentor.ai_mentor_be.entity.NotificationStage;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    boolean existsByReferenceIdAndStage(
            UUID referenceId,
            NotificationStage stage
    );

}