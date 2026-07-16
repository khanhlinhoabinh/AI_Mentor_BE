package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.ActivityLogResponse;
import com.aimentor.ai_mentor_be.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.aimentor.ai_mentor_be.service.ActivityLogSSEService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService service;

    private final ActivityLogSSEService sseService;

    @GetMapping
    public List<ActivityLogResponse> getLatestLogs() {
        return service.getLatestLogs();
    }
    @GetMapping("/stream")
    public SseEmitter stream() {

        return sseService.subscribe();

    }
}