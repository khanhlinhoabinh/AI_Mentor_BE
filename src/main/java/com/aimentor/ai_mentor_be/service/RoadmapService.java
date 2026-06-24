package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.CreateRoadmapRequest;
import com.aimentor.ai_mentor_be.dto.RoadmapResponse;
import com.aimentor.ai_mentor_be.dto.UpdateRoadmapRequest;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.RoadmapRepository;
import com.aimentor.ai_mentor_be.repository.SubjectRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aimentor.ai_mentor_be.repository.RoadmapTaskRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private void updateProgress(
            Roadmap roadmap
    ) {

        long total =
                roadmapTaskRepository
                        .countByRoadmap(
                                roadmap
                        );

        if (total == 0) {

            roadmap.setProgressPercent(
                    0.0
            );

            roadmapRepository.save(
                    roadmap
            );

            return;
        }

        long completed =
                roadmapTaskRepository
                        .countByRoadmapAndStatus(
                                roadmap,
                                RoadmapTaskStatus.COMPLETED
                        );

        double progress =
                (completed * 100.0) / total;

        roadmap.setProgressPercent(
                progress
        );

        if (progress == 100) {

            roadmap.setStatus(
                    RoadmapStatus.COMPLETED
            );
        }

        roadmapRepository.save(
                roadmap
        );
    }

    public RoadmapResponse createRoadmap(
            UUID userId,
            CreateRoadmapRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject =
                subjectRepository.findById(
                                request.getSubjectId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Subject not found"));

        if (!subject.getUser()
                .getUserId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission"
            );
        }

        Roadmap roadmap = Roadmap.builder()
                .user(user)
                .subject(subject)
                .roadmapTitle(request.getRoadmapTitle())
                .learningGoal(request.getLearningGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .progressPercent(0.0)
                .status(RoadmapStatus.NOT_STARTED)
                .createdAt(
                        new Timestamp(
                                System.currentTimeMillis()
                        )
                )
                .build();

        roadmapRepository.save(roadmap);

        return mapToResponse(roadmap);
    }

    public List<RoadmapResponse> getRoadmapsByUser(
            UUID userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        return roadmapRepository
                .findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoadmapResponse getRoadmapDetail(
            UUID userId,
            Long roadmapId
    ) {

        Roadmap roadmap =
                roadmapRepository.findById(
                                roadmapId
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

        return mapToResponse(roadmap);
    }

    public RoadmapResponse updateRoadmap(
            UUID userId,
            Long roadmapId,
            UpdateRoadmapRequest request
    ) {

        Roadmap roadmap =
                roadmapRepository.findById(
                                roadmapId
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

        roadmap.setRoadmapTitle(
                request.getRoadmapTitle()
        );

        roadmap.setLearningGoal(
                request.getLearningGoal()
        );

        roadmap.setStartDate(
                request.getStartDate()
        );

        roadmap.setEndDate(
                request.getEndDate()
        );

        roadmapRepository.save(roadmap);

        return mapToResponse(roadmap);
    }

    public void deleteRoadmap(
            UUID userId,
            Long roadmapId
    ) {

        Roadmap roadmap =
                roadmapRepository.findById(
                                roadmapId
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

        roadmapRepository.delete(roadmap);
    }

    private RoadmapResponse mapToResponse(
            Roadmap roadmap
    ) {

        return RoadmapResponse.builder()
                .roadmapId(
                        roadmap.getRoadmapId()
                )
                .subjectId(
                        roadmap.getSubject()
                                .getSubjectId()
                )
                .subjectName(
                        roadmap.getSubject()
                                .getSubjectName()
                )
                .roadmapTitle(
                        roadmap.getRoadmapTitle()
                )
                .learningGoal(
                        roadmap.getLearningGoal()
                )
                .startDate(
                        roadmap.getStartDate()
                )
                .endDate(
                        roadmap.getEndDate()
                )
                .progressPercent(
                        roadmap.getProgressPercent()
                )
                .status(
                        roadmap.getStatus()
                )
                .createdAt(
                        roadmap.getCreatedAt()
                )
                .build();
    }
    public RoadmapResponse startRoadmap(
            UUID userId,
            Long roadmapId
    ) {

        Roadmap roadmap =
                roadmapRepository.findById(
                                roadmapId
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

        roadmap.setStatus(
                RoadmapStatus.IN_PROGRESS
        );

        roadmapRepository.save(
                roadmap
        );

        return mapToResponse(
                roadmap
        );
    }
}