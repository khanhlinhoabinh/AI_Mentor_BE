package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Roadmap;
import com.aimentor.ai_mentor_be.entity.RoadmapTask;
import com.aimentor.ai_mentor_be.entity.RoadmapTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapTaskRepository
        extends JpaRepository<RoadmapTask, Long> {

    List<RoadmapTask> findByRoadmap(
            Roadmap roadmap
    );

    long countByRoadmap(
            Roadmap roadmap
    );

    long countByRoadmapAndStatus(
            Roadmap roadmap,
            RoadmapTaskStatus status
    );
}