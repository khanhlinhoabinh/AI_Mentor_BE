package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.UploadDocumentResponse;
import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.DocumentRepository;
import com.aimentor.ai_mentor_be.repository.SubjectRepository;
import com.aimentor.ai_mentor_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final SubjectRepository subjectRepository;

    private final UserRepository userRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public UploadDocumentResponse uploadDocument(
            UUID userId,
            Long subjectId,
            MultipartFile file
    ) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new RuntimeException("Invalid file");
        }

        String extension =
                fileName.substring(
                        fileName.lastIndexOf(".") + 1
                ).toLowerCase();

        if (!List.of("pdf", "doc", "docx")
                .contains(extension)) {

            throw new RuntimeException(
                    "Only PDF, DOC, DOCX allowed"
            );
        }

        File folder = new File(uploadPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String savedFileName =
                System.currentTimeMillis()
                        + "_"
                        + fileName;

        String filePath =
                uploadPath
                        + File.separator
                        + savedFileName;

        file.transferTo(
                new File(filePath)
        );

        Document document = Document.builder()
                .subject(subject)
                .uploadedBy(user)
                .fileName(fileName)
                .fileType(extension)
                .filePath(filePath)
                .createdAt(
                        new Timestamp(System.currentTimeMillis())
                )
                .updatedAt(
                        new Timestamp(System.currentTimeMillis())
                )
                .build();

        Document saved =
                documentRepository.save(document);

        return UploadDocumentResponse.builder()
                .documentId(saved.getDocumentId())
                .subjectId(subjectId)
                .fileName(saved.getFileName())
                .fileType(saved.getFileType())
                .filePath(saved.getFilePath())
                .createdAt(saved.getCreatedAt())
                .build();
    }
    public List<UploadDocumentResponse>
    getDocumentsBySubject(Long subjectId) {

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        return documentRepository
                .findBySubject(subject)
                .stream()
                .map(doc ->
                        UploadDocumentResponse.builder()
                                .documentId(doc.getDocumentId())
                                .subjectId(subjectId)
                                .fileName(doc.getFileName())
                                .fileType(doc.getFileType())
                                .filePath(doc.getFilePath())
                                .createdAt(doc.getCreatedAt())
                                .build()
                )
                .toList();
    }
    public void deleteDocument(Long documentId) {

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"
                                ));

        File file =
                new File(document.getFilePath());

        if (file.exists()) {
            file.delete();
        }

        documentRepository.delete(document);
    }
}