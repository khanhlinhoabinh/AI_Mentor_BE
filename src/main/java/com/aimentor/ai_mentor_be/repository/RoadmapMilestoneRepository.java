package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.RoadmapMilestone;
import com.aimentor.ai_mentor_be.entity.RoadmapTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapMilestoneRepository
        extends JpaRepository<RoadmapMilestone, Long> {

    List<RoadmapMilestone> findByRoadmapTask(
            RoadmapTask roadmapTask
    );
}