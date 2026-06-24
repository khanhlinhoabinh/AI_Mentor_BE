package com.aimentor.ai_mentor_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roadmaps")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Long roadmapId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "roadmap_title")
    private String roadmapTitle;

    @Column(name = "learning_goal")
    private String learningGoal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "progress_percent")
    private Double progressPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoadmapStatus status;

    @Column(name = "created_at")
    private Timestamp createdAt;
}