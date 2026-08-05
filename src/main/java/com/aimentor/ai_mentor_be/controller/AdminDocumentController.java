package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.DocumentResponse;
import com.aimentor.ai_mentor_be.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentService documentService;

    // ADMIN: Lấy tất cả tài liệu vi phạm toàn hệ thống
    @GetMapping("/flagged")
    public ResponseEntity<List<DocumentResponse>> getFlagged() {
        return ResponseEntity.ok(documentService.getFlaggedDocuments());
    }
}