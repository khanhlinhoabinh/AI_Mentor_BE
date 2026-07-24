package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.CreateMilestoneRequest;
import com.aimentor.ai_mentor_be.dto.RoadmapMilestoneResponse;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapMilestoneService {

    private final RoadmapTaskRepository taskRepository;
    private final RoadmapMilestoneRepository milestoneRepository;

    public RoadmapMilestoneResponse createMilestone(
            UUID userId,
            Long taskId,
            CreateMilestoneRequest request
    ) {

        RoadmapTask task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        verifyOwnership(task, userId);

        RoadmapMilestone milestone =
                RoadmapMilestone.builder()
                        .roadmapTask(task)
                        .milestoneTitle(
                                request.getMilestoneTitle()
                        )
                        .dueDate(
                                request.getDueDate()
                        )
                        .status(RoadmapStatus.NOT_STARTED)
                        .build();

        RoadmapMilestone saved =
                milestoneRepository.save(milestone);

        return mapToResponse(saved);
    }

    // MỚI: lấy danh sách milestone theo task, để FE không phải hardcode nữa
    public List<RoadmapMilestoneResponse> getMilestonesByTask(
            UUID userId,
            Long taskId
    ) {

        RoadmapTask task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));
        verifyOwnership(task, userId);

        return milestoneRepository
                .findByRoadmapTask(task)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // MỚI: cập nhật status milestone, có kiểm tra quyền sở hữu
    public RoadmapMilestoneResponse updateStatus(
            UUID userId,
            Long milestoneId,
            RoadmapStatus status
    ) {

        RoadmapMilestone milestone =
                milestoneRepository.findById(milestoneId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Milestone not found"
                                ));

        verifyOwnership(milestone.getRoadmapTask(), userId);

        milestone.setStatus(status);

        RoadmapMilestone saved =
                milestoneRepository.save(milestone);

        return mapToResponse(saved);
    }

    private void verifyOwnership(
            RoadmapTask task,
            UUID userId
    ) {

        if (!task.getRoadmap()
                .getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }
    }

    private RoadmapMilestoneResponse mapToResponse(
            RoadmapMilestone milestone
    ) {

        return RoadmapMilestoneResponse.builder()
                .milestoneId(milestone.getMilestoneId())
                .taskId(milestone.getRoadmapTask().getTaskId())
                .milestoneTitle(milestone.getMilestoneTitle())
                .dueDate(milestone.getDueDate())
                .status(milestone.getStatus())
                .build();
    }
}