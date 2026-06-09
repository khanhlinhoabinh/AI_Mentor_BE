// src/main/java/com/aimentor/ai_mentor_be/service/PdfAnnotationService.java
package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.AnnotationRequest;
import com.aimentor.ai_mentor_be.dto.AnnotationResponse;
import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.PdfAnnotation;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.DocumentRepository;
import com.aimentor.ai_mentor_be.repository.PdfAnnotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfAnnotationService {

    private final PdfAnnotationRepository annotationRepository;
    private final DocumentRepository documentRepository;

    // ===== LƯU hoặc CẬP NHẬT annotations =====
    public AnnotationResponse saveAnnotations(
            User currentUser,
            Long documentId,
            AnnotationRequest request
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Kiểm tra quyền
        if (!document.getUploadedBy().getUserId()
                .equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        // Tìm bản ghi cũ hoặc tạo mới (upsert)
        PdfAnnotation annotation = annotationRepository
                .findByDocumentAndUser(document, currentUser)
                .orElse(PdfAnnotation.builder()
                        .document(document)
                        .user(currentUser)
                        .build());

        annotation.setAnnotationsJson(request.getAnnotationsJson());
        return mapToResponse(annotationRepository.save(annotation));
    }

    // ===== LẤY annotations =====
    public AnnotationResponse getAnnotations(
            User currentUser,
            Long documentId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId()
                .equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        return annotationRepository
                .findByDocumentAndUser(document, currentUser)
                .map(this::mapToResponse)
                // Chưa có annotation thì trả về rỗng
                .orElse(AnnotationResponse.builder()
                        .documentId(documentId)
                        .annotationsJson("[]")
                        .build());
    }

    private AnnotationResponse mapToResponse(PdfAnnotation a) {
        return AnnotationResponse.builder()
                .annotationId(a.getAnnotationId())
                .documentId(a.getDocument().getDocumentId())
                .annotationsJson(a.getAnnotationsJson())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}