package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateMilestoneRequest;
import com.aimentor.ai_mentor_be.dto.RoadmapMilestoneResponse;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.RoadmapMilestoneService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class RoadmapMilestoneController {

    private final RoadmapMilestoneService service;

    @PostMapping("/{taskId}")
    public ResponseEntity<RoadmapMilestoneResponse>
    createMilestone(
            @PathVariable Long taskId,
            @RequestBody CreateMilestoneRequest request
    ) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                service.createMilestone(
                        user.getUserId(),
                        taskId,
                        request
                )
        );
    }
    // MỚI
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<RoadmapMilestoneResponse>>
    getMilestonesByTask(
            @PathVariable Long taskId
    ) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();


        return ResponseEntity.ok(
                service.getMilestonesByTask(
                        user.getUserId(),   // <-- thiếu tham số này
                        taskId
                )
        );
    }
}
