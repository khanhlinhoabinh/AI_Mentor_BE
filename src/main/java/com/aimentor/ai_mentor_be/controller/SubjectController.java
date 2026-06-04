package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.CreateSubjectRequest;
import com.aimentor.ai_mentor_be.dto.SubjectResponse;
import com.aimentor.ai_mentor_be.dto.UpdateSubjectRequest;
import com.aimentor.ai_mentor_be.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // CREATE SUBJECT
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @RequestHeader("userId") UUID userId,
            @RequestBody CreateSubjectRequest request
    ) {

        return ResponseEntity.ok(
                subjectService.createSubject(
                        userId,
                        request
                )
        );
    }

    // GET ALL SUBJECTS
    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getSubjects(
            @RequestHeader("userId") UUID userId
    ) {

        return ResponseEntity.ok(
                subjectService.getSubjectsByUser(
                        userId
                )
        );
    }

    // GET SUBJECT DETAIL
    @GetMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> getSubjectDetail(
            @RequestHeader("userId") UUID userId,
            @PathVariable Long subjectId
    ) {

        return ResponseEntity.ok(
                subjectService.getSubjectDetail(
                        userId,
                        subjectId
                )
        );
    }

    // UPDATE SUBJECT
    @PutMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @RequestHeader("userId") UUID userId,
            @PathVariable Long subjectId,
            @RequestBody UpdateSubjectRequest request
    ) {

        return ResponseEntity.ok(
                subjectService.updateSubject(
                        userId,
                        subjectId,
                        request
                )
        );
    }

    // DELETE SUBJECT
    @DeleteMapping("/{subjectId}")
    public ResponseEntity<String> deleteSubject(
            @RequestHeader("userId") UUID userId,
            @PathVariable Long subjectId
    ) {

        subjectService.deleteSubject(
                userId,
                subjectId
        );

        return ResponseEntity.ok(
                "Delete subject successfully"
        );
    }
}