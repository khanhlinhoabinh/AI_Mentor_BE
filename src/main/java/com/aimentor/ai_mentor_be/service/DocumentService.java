package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.DocumentCountResponse;
import com.aimentor.ai_mentor_be.dto.DocumentResponse;
import com.aimentor.ai_mentor_be.dto.EditDocumentRequest;
import com.aimentor.ai_mentor_be.entity.Document;
import com.aimentor.ai_mentor_be.entity.Subject;
import com.aimentor.ai_mentor_be.entity.User;
import com.aimentor.ai_mentor_be.repository.DocumentRepository;
import com.aimentor.ai_mentor_be.repository.PdfAnnotationRepository;
import com.aimentor.ai_mentor_be.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final SubjectRepository subjectRepository;
    private final PdfAnnotationRepository annotationRepository;

    @Value("${upload.path}")
    private String uploadPath;

    // ===================== UPLOAD =====================
    public DocumentResponse uploadDocument(
            User currentUser,
            Long subjectId,
            MultipartFile file
    ) throws IOException {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        String originalName = file.getOriginalFilename();
        String fileType = detectFileType(originalName);

        if (fileType == null) {
            throw new RuntimeException(
                    "Only PDF (.pdf) and Word (.docx) files are allowed");
        }

        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String storedFileName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadDir.resolve(storedFileName);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String extractedText = extractText(targetPath.toFile(), fileType);

        Document document = Document.builder()
                .subject(subject)
                .uploadedBy(currentUser)
                .fileName(originalName)
                .fileType(fileType)
                .filePath(targetPath.toString())
                .extractedText(extractedText)
                .status("UPLOADED")
                .build();

        return mapToResponse(documentRepository.save(document));
    }

    // ===================== GET LIST =====================
    public List<DocumentResponse> getDocumentsBySubject(
            User currentUser,
            Long subjectId
    ) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        return documentRepository.findBySubject(subject)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===================== DELETE =====================
    @Transactional
    public void deleteDocument(
            User currentUser,
            Long documentId
    ) throws IOException {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        // ✅ Xóa annotations trước để tránh foreign key constraint
        annotationRepository.deleteByDocument(document);

        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);
        documentRepository.delete(document);
    }

    // ===================== VIEW → SEEN =====================
    public DocumentResponse viewDocument(
            User currentUser,
            Long documentId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        // EDITED quan trọng hơn SEEN, không override
        if (!"EDITED".equals(document.getStatus())) {
            document.setStatus("SEEN");
        }

        document.setLastViewedAt(new Timestamp(System.currentTimeMillis()));
        return mapToResponse(documentRepository.save(document));
    }

    // ===================== EDIT WORD =====================
    public DocumentResponse editDocument(
            User currentUser,
            Long documentId,
            EditDocumentRequest request
    ) throws IOException {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        if (!"DOCX".equals(document.getFileType())) {
            throw new RuntimeException(
                    "Only DOCX files can be edited. PDF is read-only.");
        }

        File file = new File(document.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("File not found on server");
        }

        try (XWPFDocument docx = new XWPFDocument(
                Files.newInputStream(file.toPath()))) {

            // Xóa toàn bộ paragraph cũ
            int size = docx.getParagraphs().size();
            for (int i = size - 1; i >= 0; i--) {
                docx.removeBodyElement(i);
            }

            // Ghi nội dung mới
            String[] lines = request.getContent().split("\n");
            for (String line : lines) {
                XWPFParagraph para = docx.createParagraph();
                para.createRun().setText(line);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                docx.write(fos);
            }
        }

        document.setExtractedText(request.getContent());
        document.setStatus("EDITED");
        document.setLastEditedAt(new Timestamp(System.currentTimeMillis()));
        return mapToResponse(documentRepository.save(document));
    }

    // ===================== COUNT =====================
    public DocumentCountResponse countDocumentsBySubject(
            User currentUser,
            Long subjectId
    ) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        long total = documentRepository.countBySubject(subject);
        long totalPdf = documentRepository.countBySubjectAndFileType(subject, "PDF");
        long totalDocx = documentRepository.countBySubjectAndFileType(subject, "DOCX");

        return DocumentCountResponse.builder()
                .subjectId(subject.getSubjectId())
                .subjectName(subject.getSubjectName())
                .totalDocuments(total)
                .totalPdf(totalPdf)
                .totalDocx(totalDocx)
                .build();
    }

    // ===================== HELPERS =====================
    private String detectFileType(String fileName) {
        if (fileName == null) return null;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf"))  return "PDF";
        if (lower.endsWith(".docx")) return "DOCX";
        return null;
    }

    private String extractText(File file, String fileType) {
        try {
            return switch (fileType) {
                case "PDF"  -> extractFromPdf(file);
                case "DOCX" -> extractFromDocx(file);
                default     -> "";
            };
        } catch (Exception e) {
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

    // ✅ mapToResponse đầy đủ 3 fields mới
    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getDocumentId())
                .subjectId(document.getSubject().getSubjectId())
                .subjectName(document.getSubject().getSubjectName())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .filePath(document.getFilePath())
                .extractedText(document.getExtractedText())
                .status(document.getStatus())
                .lastViewedAt(document.getLastViewedAt())
                .lastEditedAt(document.getLastEditedAt())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
    // Lấy entity để serve file — dùng cho endpoint download
    public Document getDocumentEntity(User currentUser, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId()
                .equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        return document;
    }

}