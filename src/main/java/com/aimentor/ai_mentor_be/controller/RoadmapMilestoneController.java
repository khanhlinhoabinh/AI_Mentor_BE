package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateMilestoneRequest;
import com.aimentor.ai_mentor_be.entity.RoadmapMilestone;
import com.aimentor.ai_mentor_be.service.RoadmapMilestoneService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class RoadmapMilestoneController {

    private final RoadmapMilestoneService service;

    @PostMapping("/{taskId}")
    public ResponseEntity<RoadmapMilestone>
    createMilestone(
            @PathVariable Long taskId,
            @RequestBody CreateMilestoneRequest request
    ) {

        return ResponseEntity.ok(
                service.createMilestone(
                        taskId,
                        request
                )
        );
    }
}