package com.aimentor.ai_mentor_be.service;

import com.aimentor.ai_mentor_be.dto.*;
import com.aimentor.ai_mentor_be.entity.*;
import com.aimentor.ai_mentor_be.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository      documentRepository;
    private final SubjectRepository       subjectRepository;
    private final PdfAnnotationRepository annotationRepository;
    private final ActivityLogService      activityLogService;
    private final ModerationService       moderationService;    // ✅ thêm
    private final NotificationService     notificationService;  // ✅ thêm
    private final UserRepository          userRepository;       // ✅ thêm

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

        // ✅ BƯỚC MỚI: Kiểm duyệt nội dung bằng AI
        ModerationResult moderation = runModeration(extractedText, originalName);

        // Build document với kết quả kiểm duyệt
        Document document = Document.builder()
                .subject(subject)
                .uploadedBy(currentUser)
                .fileName(originalName)
                .fileType(fileType)
                .filePath(targetPath.toString())
                .extractedText(extractedText)
                .status("UPLOADED")
                // ✅ Lưu kết quả kiểm duyệt
                .moderationRiskLevel(
                        moderation.getRiskLevel() != null
                                ? moderation.getRiskLevel() : "NONE")
                .hasViolation(
                        moderation.getHasViolation() != null
                                ? moderation.getHasViolation() : false)
                .moderationSummary(moderation.getSummary())
                .moderationWarning(moderation.getWarning())
                .build();

        Document saved = documentRepository.save(document);

        // ✅ Nếu vi phạm → gửi thông báo cho admin
        if (Boolean.TRUE.equals(moderation.getHasViolation())) {
            sendModerationNotificationToAdmins(saved, currentUser, moderation);
        }

        return mapToResponse(saved);
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
    public void deleteDocument(User currentUser, Long documentId) throws IOException {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        annotationRepository.deleteByDocument(document);
        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);
        documentRepository.delete(document);
    }

    // ===================== VIEW → SEEN =====================
    public DocumentResponse viewDocument(User currentUser, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        if (!"EDITED".equals(document.getStatus())) {
            document.setStatus("SEEN");
        }

        document.setLastViewedAt(new Timestamp(System.currentTimeMillis()));
        return mapToResponse(documentRepository.save(document));
    }

    // ===================== EDIT WORD =====================
    public DocumentResponse editDocument(
            User currentUser, Long documentId, EditDocumentRequest request
    ) throws IOException {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        if (!"DOCX".equals(document.getFileType())) {
            throw new RuntimeException("Only DOCX files can be edited.");
        }

        document.setExtractedText(request.getContent());
        document.setStatus("EDITED");
        document.setLastEditedAt(new Timestamp(System.currentTimeMillis()));
        return mapToResponse(documentRepository.save(document));
    }

    // ===================== COUNT =====================
    public DocumentCountResponse countDocumentsBySubject(
            User currentUser, Long subjectId
    ) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        long total     = documentRepository.countBySubject(subject);
        long totalPdf  = documentRepository.countBySubjectAndFileType(subject, "PDF");
        long totalDocx = documentRepository.countBySubjectAndFileType(subject, "DOCX");

        return DocumentCountResponse.builder()
                .subjectId(subject.getSubjectId())
                .subjectName(subject.getSubjectName())
                .totalDocuments(total)
                .totalPdf(totalPdf)
                .totalDocx(totalDocx)
                .build();
    }

    // ===================== GET ENTITY =====================
    public Document getDocumentEntity(User currentUser, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUploadedBy().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }
        return document;
    }

    // ===================== TẠO TÀI LIỆU TRỐNG =====================
    public DocumentResponse createEmptyDocument(
            User currentUser, Long subjectId, CreateEmptyDocumentRequest request
    ) throws IOException {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (!subject.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("You do not have permission");
        }

        String fileName = request.getFileName();
        if (fileName == null || fileName.isBlank()) fileName = "Tài liệu mới";
        if (!fileName.toLowerCase().endsWith(".docx")) fileName = fileName + ".docx";

        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String storedName = UUID.randomUUID() + "_" + fileName;
        Path targetPath   = uploadDir.resolve(storedName);

        try (XWPFDocument emptyDoc = new XWPFDocument()) {
            emptyDoc.createParagraph();
            try (java.io.FileOutputStream fos =
                         new java.io.FileOutputStream(targetPath.toFile())) {
                emptyDoc.write(fos);
            }
        }

        Document document = Document.builder()
                .subject(subject)
                .uploadedBy(currentUser)
                .fileName(fileName)
                .fileType("DOCX")
                .filePath(targetPath.toString())
                .extractedText("")
                .status("UPLOADED")
                .build();

        return mapToResponse(documentRepository.save(document));
    }

    // ═══════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════

    // ✅ Chạy kiểm duyệt — nếu lỗi thì không block upload
    private ModerationResult runModeration(String text, String fileName) {
        try {
            if (text == null || text.isBlank()) {
                return safeModerationResult();
            }
            return moderationService.moderate(text);
        } catch (Exception e) {
            log.warn("Moderation failed for file '{}': {}", fileName, e.getMessage());
            return safeModerationResult();
        }
    }

    private ModerationResult safeModerationResult() {
        return ModerationResult.builder()
                .safe(true)
                .riskLevel("NONE")
                .hasViolation(false)
                .categories(java.util.List.of())
                .summary("Không thể phân tích nội dung.")
                .warning("")
                .build();
    }

    // ✅ Gửi notification đến tất cả admin
    private void sendModerationNotificationToAdmins(
            Document document,
            User uploader,
            ModerationResult moderation
    ) {
        try {
            List<User> admins = userRepository.findByRoleRoleName("ADMIN");

            if (admins.isEmpty()) {
                log.warn("No admin found to send moderation notification");
                return;
            }

            String riskLabel = switch (moderation.getRiskLevel()) {
                case "HIGH"   -> "🔴 CAO";
                case "MEDIUM" -> "🟡 TRUNG BÌNH";
                case "LOW"    -> "🟢 THẤP";
                default       -> moderation.getRiskLevel();
            };

            String title = "⚠️ Cảnh báo tài liệu vi phạm";

            String content = String.format(
                    """
                    Tài liệu "%s" được upload bởi %s (%s) có dấu hiệu vi phạm tiêu chuẩn cộng đồng.
                    
                    Mức độ rủi ro: %s
                    Tóm tắt: %s
                    Cảnh báo: %s
                    
                    Vui lòng kiểm tra và xử lý tài liệu này.
                    """,
                    document.getFileName(),
                    uploader.getFullName(),
                    uploader.getEmail(),
                    riskLabel,
                    moderation.getSummary(),
                    moderation.getWarning()
            );

            for (User admin : admins) {
                notificationService.createNotification(
                        admin,
                        title,
                        content,
                        NotificationType.DOCUMENT_MODERATION,
                        null,
                        "DOCUMENT:" + document.getDocumentId(),
                        NotificationStage.MODERATION_WARNING
                );
            }

            log.info("Sent moderation notification to {} admins for document '{}'",
                    admins.size(), document.getFileName());

        } catch (Exception e) {
            log.error("Failed to send moderation notification: {}", e.getMessage());
        }
    }

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
        try (XWPFDocument docx = new XWPFDocument(Files.newInputStream(file.toPath()))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : docx.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    // ✅ mapToResponse đầy đủ bao gồm moderation fields
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
                // ✅ Moderation fields
                .moderationRiskLevel(document.getModerationRiskLevel())
                .hasViolation(document.getHasViolation())
                .moderationSummary(document.getModerationSummary())
                .moderationWarning(document.getModerationWarning())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
    // ===================== ADMIN: LẤY TÀI LIỆU VI PHẠM =====================
    public List<DocumentResponse> getFlaggedDocuments() {
        return documentRepository
                .findByHasViolationTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}