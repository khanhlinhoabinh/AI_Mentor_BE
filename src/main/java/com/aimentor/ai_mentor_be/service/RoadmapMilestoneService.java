package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.CreateMilestoneRequest;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoadmapMilestoneService {

    private final RoadmapTaskRepository taskRepository;
    private final RoadmapMilestoneRepository milestoneRepository;

    public RoadmapMilestone createMilestone(
            Long taskId,
            CreateMilestoneRequest request
    ) {

        RoadmapTask task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        RoadmapMilestone milestone =
                RoadmapMilestone.builder()
                        .roadmapTask(task)
                        .milestoneTitle(
                                request.getMilestoneTitle()
                        )
                        .dueDate(
                                request.getDueDate()
                        )
                        .completed(false)
                        .build();

        return milestoneRepository.save(
                milestone
        );
    }
}