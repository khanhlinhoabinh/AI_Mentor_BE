package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateSubjectRequest;
import com.aimentor.ai_mentor_be.dto.SubjectResponse;
import com.aimentor.ai_mentor_be.dto.UpdateSubjectRequest;
import com.aimentor.ai_mentor_be.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aimentor.ai_mentor_be.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // CREATE SUBJECT
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @RequestBody CreateSubjectRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                subjectService.createSubject(
                        user.getUserId(),
                        request
                )
        );
    }

    // GET ALL SUBJECTS
    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getSubjects() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                subjectService.getSubjectsByUser(
                        user.getUserId()
                )
        );
    }

    // GET SUBJECT DETAIL
    @GetMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> getSubjectDetail(
            @PathVariable Long subjectId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                subjectService.getSubjectDetail(
                        user.getUserId(),
                        subjectId
                )
        );
    }

    // UPDATE SUBJECT
    @PutMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody UpdateSubjectRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                subjectService.updateSubject(
                        user.getUserId(),
                        subjectId,
                        request
                )
        );
    }

    // DELETE SUBJECT
    @DeleteMapping("/{subjectId}")
    public ResponseEntity<String> deleteSubject(
            @PathVariable Long subjectId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user =
                (User) authentication.getPrincipal();

        subjectService.deleteSubject(
                user.getUserId(),
                subjectId
        );

        return ResponseEntity.ok(
                "Delete subject successfully"
        );
    }
}