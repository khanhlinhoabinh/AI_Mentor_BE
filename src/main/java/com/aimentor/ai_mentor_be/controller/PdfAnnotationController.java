// src/main/java/com/aimentor/ai_mentor_be/controller/PdfAnnotationController.java
package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.AnnotationRequest;
import com.aimentor.ai_mentor_be.dto.AnnotationResponse;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.PdfAnnotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects/{subjectId}/documents/{documentId}/annotations")
@RequiredArgsConstructor
public class PdfAnnotationController {

    private final PdfAnnotationService annotationService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // GET — lấy annotations khi mở file
    @GetMapping
    public ResponseEntity<AnnotationResponse> get(
            @PathVariable Long subjectId,
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(
                annotationService.getAnnotations(getCurrentUser(), documentId));
    }

    // PUT — lưu/cập nhật annotations
    @PutMapping
    public ResponseEntity<AnnotationResponse> save(
            @PathVariable Long subjectId,
            @PathVariable Long documentId,
            @RequestBody AnnotationRequest request
    ) {
        return ResponseEntity.ok(
                annotationService.saveAnnotations(
                        getCurrentUser(), documentId, request));
    }
} 