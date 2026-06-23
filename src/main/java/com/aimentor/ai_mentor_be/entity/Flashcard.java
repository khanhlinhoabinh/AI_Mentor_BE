package com.aimentor.ai_mentor_be.entity;

import com.aimentor.ai_mentor_be.entity.enums.FlashcardType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_id")
    private Long flashcardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private FlashcardType cardType;

    @Column(name = "front_content", columnDefinition = "TEXT", nullable = false)
    private String frontContent;

    @Column(name = "back_content", columnDefinition = "TEXT", nullable = false)
    private String backContent;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

        if (displayOrder == null) {
            displayOrder = 0;
        }
    }
}