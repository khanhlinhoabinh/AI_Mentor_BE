package com.aimentor.ai_mentor_be.controller;

import com.aimentor.ai_mentor_be.dto.UploadDocumentResponse;
import com.aimentor.ai_mentor_be.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{subjectId}")
    public ResponseEntity<UploadDocumentResponse>
    uploadDocument(

            @RequestHeader("userId")
            UUID userId,

            @PathVariable
            Long subjectId,

            @RequestParam("file")
            MultipartFile file

    ) throws Exception {

        return ResponseEntity.ok(
                documentService.uploadDocument(
                        userId,
                        subjectId,
                        file
                )
        );
    }
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<UploadDocumentResponse>>
    getDocuments(
            @PathVariable Long subjectId
    ) {

        return ResponseEntity.ok(
                documentService.getDocumentsBySubject(
                        subjectId
                )
        );
    }
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String>
    deleteDocument(
            @PathVariable Long documentId
    ) {

        documentService.deleteDocument(
                documentId
        );

        return ResponseEntity.ok(
                "Delete document successfully"
        );
    }
}