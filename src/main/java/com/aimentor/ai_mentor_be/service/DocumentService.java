package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.DocumentResponse;
import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.DocumentRepository;
import com.aimentor.ai_mentor_be.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final SubjectRepository subjectRepository;

    // Khớp với key trong application.properties của bạn
    @Value("${upload.path}")
    private String uploadPath;

    // ===================== UPLOAD =====================
    public DocumentResponse uploadDocument(
            User currentUser,
            Long subjectId,
            MultipartFile file
    ) throws IOException {

        // 1. Kiểm tra môn học tồn tại và thuộc về user
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId()
                .equals(currentUser.getUserId())) {
            throw new RuntimeException(
                    "You do not have permission to upload to this subject");
        }

        // 2. Kiểm tra định dạng file (chỉ cho PDF và DOCX)
        String originalName = file.getOriginalFilename();
        String fileType = detectFileType(originalName);

        if (fileType == null) {
            throw new RuntimeException(
                    "Only PDF (.pdf) and Word (.docx) files are allowed");
        }

        // 3. Tạo thư mục uploads nếu chưa tồn tại
        Path uploadDir = Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(uploadDir);

        // 4. Tạo tên file duy nhất để tránh trùng
        String storedFileName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadDir.resolve(storedFileName);

        // 5. Lưu file xuống disk
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 6. Extract text (để dùng cho AI sau này)
        String extractedText = extractText(
                targetPath.toFile(), fileType);

        // 7. Lưu thông tin vào DB
        Document document = Document.builder()
                .subject(subject)
                .uploadedBy(currentUser)
                .fileName(originalName)
                .fileType(fileType)
                .filePath(targetPath.toString())
                .extractedText(extractedText)
                .build();

        return mapToResponse(documentRepository.save(document));
    }

    // ===================== GET LIST =====================
    public List<DocumentResponse> getDocumentsBySubject(
            User currentUser,
            Long subjectId
    ) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId()
                .equals(currentUser.getUserId())) {
            throw new RuntimeException(
                    "You do not have permission");
        }

        return documentRepository.findBySubject(subject)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===================== DELETE =====================
    public void deleteDocument(
            User currentUser,
            Long documentId
    ) throws IOException {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found"));

        // Chỉ người upload mới được xóa
        if (!document.getUploadedBy().getUserId()
                .equals(currentUser.getUserId())) {
            throw new RuntimeException(
                    "You do not have permission to delete this document");
        }

        // Xóa file vật lý trên disk
        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);

        // Xóa record trong DB
        documentRepository.delete(document);
    }

    // ===================== PRIVATE HELPERS =====================

    // Nhận diện loại file qua đuôi mở rộng
    private String detectFileType(String fileName) {
        if (fileName == null) return null;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf"))  return "PDF";
        if (lower.endsWith(".docx")) return "DOCX";
        return null; // không hợp lệ
    }

    // Điều phối extract text theo loại file
    private String extractText(File file, String fileType) {
        try {
            return switch (fileType) {
                case "PDF"  -> extractFromPdf(file);
                case "DOCX" -> extractFromDocx(file);
                default     -> "";
            };
        } catch (Exception e) {
            // Không extract được thì để trống, không block upload
            return "";
        }
    }

    private String extractFromPdf(File file) throws IOException {
        try (PDDocument pd = PDDocument.load(file)) {
            return new PDFTextStripper().getText(pd);
        }
    }

    private String extractFromDocx(File file) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(
                Files.newInputStream(file.toPath()))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : docx.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    // Entity -> DTO
    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getDocumentId())
                .subjectId(document.getSubject().getSubjectId())
                .subjectName(document.getSubject().getSubjectName())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .filePath(document.getFilePath())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}