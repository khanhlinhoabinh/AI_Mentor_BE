package com.aimentor.ai_mentor_be.repository;

import com.aimentor.ai_mentor_be.entity.Roadmap;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapRepository
        extends JpaRepository<Roadmap, Long> {

    List<Roadmap> findByUser(User user);

}