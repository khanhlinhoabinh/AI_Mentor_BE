package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateRoadmapRequest;
import com.aimentor.ai_mentor_be.dto.RoadmapResponse;
import com.aimentor.ai_mentor_be.dto.UpdateRoadmapRequest;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.aimentor.ai_mentor_be.dto.AiRoadmapStageSuggestion;
import com.aimentor.ai_mentor_be.dto.AiRoadmapSuggestionRequest;
import com.aimentor.ai_mentor_be.service.AiRoadmapService;

import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    private final AiRoadmapService aiRoadmapService;

    @PostMapping
    public ResponseEntity<RoadmapResponse>
    createRoadmap(
            @RequestBody
            CreateRoadmapRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                roadmapService.createRoadmap(
                        user.getUserId(),
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<RoadmapResponse>>
    getRoadmaps() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                roadmapService.getRoadmapsByUser(
                        user.getUserId()
                )
        );
    }

    @GetMapping("/{roadmapId}")
    public ResponseEntity<RoadmapResponse>
    getRoadmapDetail(
            @PathVariable Long roadmapId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                roadmapService.getRoadmapDetail(
                        user.getUserId(),
                        roadmapId
                )
        );
    }

    @PutMapping("/{roadmapId}")
    public ResponseEntity<RoadmapResponse>
    updateRoadmap(
            @PathVariable Long roadmapId,
            @RequestBody
            UpdateRoadmapRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                roadmapService.updateRoadmap(
                        user.getUserId(),
                        roadmapId,
                        request
                )
        );
    }

    @DeleteMapping("/{roadmapId}")
    public ResponseEntity<String>
    deleteRoadmap(
            @PathVariable Long roadmapId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        roadmapService.deleteRoadmap(
                user.getUserId(),
                roadmapId
        );

        return ResponseEntity.ok(
                "Delete roadmap successfully"
        );
    }
    @PostMapping("/{roadmapId}/start")
    public ResponseEntity<RoadmapResponse>
    startRoadmap(
            @PathVariable Long roadmapId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                roadmapService.startRoadmap(
                        user.getUserId(),
                        roadmapId
                )
        );
    }@PostMapping("/{roadmapId}/ai-suggest-stages")
    public ResponseEntity<List<AiRoadmapStageSuggestion>>
    suggestRoadmapStages(
                    @PathVariable Long roadmapId,
                    @RequestBody AiRoadmapSuggestionRequest request
            ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                aiRoadmapService.suggestStages(
                        user.getUserId(),
                        roadmapId,
                        request
                )
        );
    }

}