package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.flashcard.*;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping("/api/flashcard-sets/{setId}/cards")
    public ResponseEntity<FlashcardResponse> create(
            @PathVariable Long setId,
            @RequestBody CreateFlashcardRequest request
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardService.create(
                        user.getUserId(),
                        setId,
                        request
                )
        );
    }

    @GetMapping("/api/flashcard-sets/{setId}/cards")
    public ResponseEntity<List<FlashcardResponse>> getBySet(
            @PathVariable Long setId
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardService.getBySet(
                        user.getUserId(),
                        setId
                )
        );
    }

    @GetMapping("/api/flashcards/{cardId}")
    public ResponseEntity<FlashcardResponse> getDetail(
            @PathVariable Long cardId
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardService.getDetail(
                        user.getUserId(),
                        cardId
                )
        );
    }

    @PutMapping("/api/flashcards/{cardId}")
    public ResponseEntity<FlashcardResponse> update(
            @PathVariable Long cardId,
            @RequestBody UpdateFlashcardRequest request
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardService.update(
                        user.getUserId(),
                        cardId,
                        request
                )
        );
    }

    @DeleteMapping("/api/flashcards/{cardId}")
    public ResponseEntity<String> delete(
            @PathVariable Long cardId
    ) {

        User user = getCurrentUser();

        flashcardService.delete(
                user.getUserId(),
                cardId
        );

        return ResponseEntity.ok(
                "Delete flashcard successfully"
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (User) authentication.getPrincipal();
    }
}