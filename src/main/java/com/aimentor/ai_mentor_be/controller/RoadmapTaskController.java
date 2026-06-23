package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateRoadmapTaskRequest;
import com.aimentor.ai_mentor_be.dto.RoadmapTaskResponse;
import com.aimentor.ai_mentor_be.dto.UpdateRoadmapTaskRequest;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.RoadmapTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roadmap-tasks")
@RequiredArgsConstructor
public class RoadmapTaskController {

    private final RoadmapTaskService roadmapTaskService;

    @PostMapping
    public ResponseEntity<RoadmapTaskResponse> createTask(
            @RequestBody CreateRoadmapTaskRequest request
    ) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        System.out.println("========== CREATE TASK ==========");
        System.out.println("AUTH = " + auth);
        System.out.println("AUTHORITIES = " + auth.getAuthorities());
        System.out.println("PRINCIPAL = " + auth.getPrincipal());

        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                roadmapTaskService.createTask(
                        user.getUserId(),
                        request
                )
        );
    }

    @GetMapping("/{roadmapId}")
    public ResponseEntity<List<RoadmapTaskResponse>> getTasks(
            @PathVariable Long roadmapId
    ) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        System.out.println("========== GET TASKS ==========");
        System.out.println("AUTH = " + auth);
        System.out.println("AUTHORITIES = " + auth.getAuthorities());

        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                roadmapTaskService.getTasks(
                        user.getUserId(),
                        roadmapId
                )
        );
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<RoadmapTaskResponse> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateRoadmapTaskRequest request
    ) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        System.out.println("========== UPDATE TASK ==========");
        System.out.println("AUTH = " + auth);
        System.out.println("AUTHORITIES = " + auth.getAuthorities());

        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                roadmapTaskService.updateTask(
                        user.getUserId(),
                        taskId,
                        request
                )
        );
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long taskId
    ) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        System.out.println("========== DELETE TASK ==========");
        System.out.println("AUTH = " + auth);
        System.out.println("AUTHORITIES = " + auth.getAuthorities());

        User user = (User) auth.getPrincipal();

        roadmapTaskService.deleteTask(
                user.getUserId(),
                taskId
        );

        return ResponseEntity.ok(
                "Delete task successfully"
        );
    }
}