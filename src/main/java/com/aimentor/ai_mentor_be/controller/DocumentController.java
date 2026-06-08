package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.DocumentCountResponse;
import com.aimentor.ai_mentor_be.dto.DocumentResponse;
import com.aimentor.ai_mentor_be.dto.EditDocumentRequest;
import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/subjects/{subjectId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    // UPLOAD
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(
                documentService.uploadDocument(getCurrentUser(), subjectId, file));
    }

    // GET LIST
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll(
            @PathVariable Long subjectId
    ) {
        return ResponseEntity.ok(
                documentService.getDocumentsBySubject(getCurrentUser(), subjectId));
    }

    // DELETE
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> delete(
            @PathVariable Long subjectId,
            @PathVariable Long documentId
    ) throws IOException {
        documentService.deleteDocument(getCurrentUser(), documentId);
        return ResponseEntity.ok("Document deleted successfully");
    }

    // ✅ VIEW → SEEN
    @PatchMapping("/{documentId}/view")
    public ResponseEntity<DocumentResponse> view(
            @PathVariable Long subjectId,
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(
                documentService.viewDocument(getCurrentUser(), documentId));
    }

    // ✅ EDIT WORD
    @PutMapping("/{documentId}/edit")
    public ResponseEntity<DocumentResponse> edit(
            @PathVariable Long subjectId,
            @PathVariable Long documentId,
            @RequestBody EditDocumentRequest request
    ) throws IOException {
        return ResponseEntity.ok(
                documentService.editDocument(getCurrentUser(), documentId, request));
    }

    // ✅ COUNT
    @GetMapping("/count")
    public ResponseEntity<DocumentCountResponse> count(
            @PathVariable Long subjectId
    ) {
        return ResponseEntity.ok(
                documentService.countDocumentsBySubject(getCurrentUser(), subjectId));
    }
    // DOWNLOAD / XEM file
    @GetMapping("/{documentId}/file")
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(
            @PathVariable Long subjectId,
            @PathVariable Long documentId
    ) throws IOException {

        Document document = documentService.getDocumentEntity(
                getCurrentUser(), documentId);

        Path filePath = Paths.get(document.getFilePath());
        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found");
        }

        // Xác định content type
        String contentType = document.getFileType().equals("PDF")
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                // inline = mở trực tiếp trên browser, attachment = tải về
                .header("Content-Disposition",
                        "inline; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }
}