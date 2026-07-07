package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.NotificationResponse;
import com.aimentor.ai_mentor_be.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.aimentor.ai_mentor_be.dto.MoodFeedbackRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication){

        return ResponseEntity.ok(
                notificationService.getMyNotifications(authentication)
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            Authentication authentication){

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(authentication)
        );
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUnread(
            Authentication authentication){

        return ResponseEntity.ok(
                notificationService.countUnread(authentication)
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable UUID id,
            Authentication authentication){

        notificationService.markAsRead(id, authentication);

        return ResponseEntity.ok("Notification marked as read.");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(
            Authentication authentication){

        notificationService.markAllAsRead(authentication);

        return ResponseEntity.ok("All notifications marked as read.");

    }
    @PostMapping("/{id}/feedback")
    public ResponseEntity<String> sendFeedbackToAI(
            @PathVariable UUID id,
            @RequestBody MoodFeedbackRequest request,
            Authentication authentication
    ){

        return ResponseEntity.ok(
                notificationService.feedbackToAI(
                        id,
                        request,
                        authentication
                )
        );
    }

}