package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateReminderRequest;
import com.aimentor.ai_mentor_be.dto.ReminderResponse;
import com.aimentor.ai_mentor_be.dto.UpdateReminderRequest;
import com.aimentor.ai_mentor_be.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @RequestBody CreateReminderRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                reminderService.createReminder(request, authentication)
        );
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getMyReminders(
            Authentication authentication) {

        return ResponseEntity.ok(
                reminderService.getMyReminders(authentication)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getReminder(
            @PathVariable UUID id,
            Authentication authentication) {

        return ResponseEntity.ok(
                reminderService.getReminder(id, authentication)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable UUID id,
            @RequestBody UpdateReminderRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                reminderService.updateReminder(id, request, authentication)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReminder(
            @PathVariable UUID id,
            Authentication authentication) {

        reminderService.deleteReminder(id, authentication);

        return ResponseEntity.ok("Reminder deleted successfully.");
    }

}