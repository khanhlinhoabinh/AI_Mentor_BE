package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.flashcard.*;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.FlashcardSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard-sets")
@RequiredArgsConstructor
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;

    @PostMapping
    public ResponseEntity<FlashcardSetResponse> create(
            @RequestBody CreateFlashcardSetRequest request
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardSetService.create(
                        user.getUserId(),
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<FlashcardSetResponse>> getAll() {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardSetService.getAll(
                        user.getUserId()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> getDetail(
            @PathVariable Long id
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardSetService.getDetail(
                        user.getUserId(),
                        id
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashcardSetResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateFlashcardSetRequest request
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardSetService.update(
                        user.getUserId(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id
    ) {

        User user = getCurrentUser();

        flashcardSetService.delete(
                user.getUserId(),
                id
        );

        return ResponseEntity.ok(
                "Delete flashcard set successfully"
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<FlashcardSetResponse>> search(
            @RequestParam String keyword
    ) {

        User user = getCurrentUser();

        return ResponseEntity.ok(
                flashcardSetService.search(
                        user.getUserId(),
                        keyword
                )
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