package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.*;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapTaskService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;

    public RoadmapTaskResponse createTask(
            UUID userId,
            CreateRoadmapTaskRequest request
    ) {

        Roadmap roadmap =
                roadmapRepository.findById(
                                request.getRoadmapId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Roadmap not found"
                                ));

        if (!roadmap.getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        RoadmapTask task = RoadmapTask.builder()
                .roadmap(roadmap)
                .taskName(request.getTaskName())
                .taskGoal(request.getTaskGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(RoadmapTaskStatus.NOT_COMPLETED)
                .build();

        roadmapTaskRepository.save(task);

        recalculateProgress(roadmap);

        return mapToResponse(task);
    }

    public List<RoadmapTaskResponse> getTasks(
            UUID userId,
            Long roadmapId
    ) {

        Roadmap roadmap =
                roadmapRepository.findById(roadmapId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Roadmap not found"
                                ));

        if (!roadmap.getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        return roadmapTaskRepository
                .findByRoadmap(roadmap)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoadmapTaskResponse updateTask(
            UUID userId,
            Long taskId,
            UpdateRoadmapTaskRequest request
    ) {

        RoadmapTask task =
                roadmapTaskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        if (!task.getRoadmap()
                .getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        task.setTaskName(
                request.getTaskName()
        );
        task.setTaskGoal(request.getTaskGoal());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setStatus(request.getStatus());

        roadmapTaskRepository.save(task);

        recalculateProgress(task.getRoadmap());

        return mapToResponse(task);
    }

    public void deleteTask(
            UUID userId,
            Long taskId
    ) {

        RoadmapTask task =
                roadmapTaskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        if (!task.getRoadmap()
                .getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        Roadmap roadmap = task.getRoadmap();

        roadmapTaskRepository.delete(task);

        recalculateProgress(roadmap);
    }

    private void recalculateProgress(
            Roadmap roadmap
    ) {

        List<RoadmapTask> tasks =
                roadmapTaskRepository.findByRoadmap(
                        roadmap
                );

        if (tasks.isEmpty()) {

            roadmap.setProgressPercent(0.0);

        } else {

            long completed =
                    tasks.stream()
                            .filter(
                                    t ->
                                            t.getStatus()
                                                    ==
                                                    RoadmapTaskStatus.COMPLETED
                            )
                            .count();

            double progress =
                    ((double) completed
                            / tasks.size())
                            * 100;

            roadmap.setProgressPercent(progress);

            if (progress == 100) {

                roadmap.setStatus(
                        RoadmapStatus.COMPLETED
                );

            } else if (progress > 0) {

                roadmap.setStatus(
                        RoadmapStatus.IN_PROGRESS
                );
            }
        }

        roadmapRepository.save(roadmap);
    }

    private RoadmapTaskResponse mapToResponse(
            RoadmapTask task
    ) {

        return RoadmapTaskResponse.builder()
                .taskId(task.getTaskId())
                .roadmapId(
                        task.getRoadmap()
                                .getRoadmapId()
                )
                .taskTitle(task.getTaskName())
                .taskGoal(task.getTaskGoal())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .status(task.getStatus())
                .build();
    }
}