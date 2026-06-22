package com.aimentor.ai_mentor_be.entity;

import com.aimentor.ai_mentor_be.entity.enums.FlashcardSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flashcard_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_set_id")
    private Long flashcardSetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "set_name", nullable = false)
    private String setName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private FlashcardSourceType sourceType;

    @Column(name = "total_cards")
    private Integer totalCards;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "flashcardSet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Flashcard> flashcards = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

        if (totalCards == null) {
            totalCards = 0;
        }
    }
}