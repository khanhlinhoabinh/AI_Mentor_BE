package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.DocumentResponse;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/subjects/{subjectId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // Lấy user hiện tại — giống hệt cách bạn dùng trong SubjectController
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        return (User) auth.getPrincipal();
    }

    // UPLOAD file PDF hoặc DOCX
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return ResponseEntity.ok(
                documentService.uploadDocument(
                        getCurrentUser(),
                        subjectId,
                        file
                )
        );
    }

    // LẤY DANH SÁCH tài liệu của một môn học
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll(
            @PathVariable Long subjectId
    ) {

        return ResponseEntity.ok(
                documentService.getDocumentsBySubject(
                        getCurrentUser(),
                        subjectId
                )
        );
    }

    // XÓA tài liệu
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> delete(
            @PathVariable Long subjectId,
            @PathVariable Long documentId
    ) throws IOException {

        documentService.deleteDocument(
                getCurrentUser(),
                documentId
        );

        return ResponseEntity.ok("Document deleted successfully");
    }
}