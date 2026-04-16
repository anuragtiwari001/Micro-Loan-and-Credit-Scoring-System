package com.gla.document_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gla.document_service.dto.DocumentResponse;
import com.gla.document_service.entity.UserDocument;
import com.gla.document_service.enums.DocumentType;
import com.gla.document_service.repository.UserDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for KYC document upload and retrieval.
 *
 * UPLOAD FLOW:
 *   1. Validate file: not empty, type allowed (PDF/JPG/PNG), size ≤ 10MB.
 *   2. Upload bytes to Cloudinary → receive secure HTTPS URL.
 *   3. Persist UserDocument entity (email, type, fileName, fileUrl).
 *   4. Return DocumentResponse (hides internal entity fields).
 *
 * ALLOWED TYPES: application/pdf, image/jpeg, image/png
 * MAX SIZE:      10MB (also enforced in application.properties for multipart)
 *
 * NOTE: Multiple uploads of the same DocumentType for the same user are
 *       allowed (e.g. resubmitting a rejected document). The admin sees all.
 */
@Service
public class DocumentService {

    private static final long   MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES =
            Arrays.asList("application/pdf", "image/jpeg", "image/png");

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserDocumentRepository repository;

    // ─────────────────────────────────────────────────────────────────────────
    // Upload
    // ─────────────────────────────────────────────────────────────────────────

    public DocumentResponse upload(String email, DocumentType type,
                                   MultipartFile file) {
        // ── Validate ──────────────────────────────────────────────────────────
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or missing");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new RuntimeException(
                    "File too large. Maximum allowed size is 10MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException(
                    "Unsupported file type: " + contentType +
                            ". Allowed: PDF, JPG, PNG.");
        }

        // ── Upload to Cloudinary ──────────────────────────────────────────────
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",         "gla-kyc/" + email,
                            "resource_type",  "auto",
                            "use_filename",   true,
                            "unique_filename", true
                    )
            );

            String secureUrl = uploadResult.get("secure_url").toString();

            // ── Persist metadata ──────────────────────────────────────────────
            UserDocument doc = new UserDocument();
            doc.setEmail(email);
            doc.setDocumentType(type);
            doc.setFileName(file.getOriginalFilename());
            doc.setFileUrl(secureUrl);

            UserDocument saved = repository.save(doc);

            return new DocumentResponse(
                    saved.getId(),
                    saved.getDocumentType().name(),
                    saved.getFileUrl());

        } catch (RuntimeException e) {
            throw e;   // re-throw our validation errors
        } catch (Exception e) {
            throw new RuntimeException("Upload to Cloudinary failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get by email (admin)
    // ─────────────────────────────────────────────────────────────────────────

    public List<DocumentResponse> getByEmail(String email) {
        return repository.findByEmail(email)
                .stream()
                .map(doc -> new DocumentResponse(
                        doc.getId(),
                        doc.getDocumentType().name(),
                        doc.getFileUrl()))
                .collect(Collectors.toList());
    }
}



//package com.gla.document_service.service;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import com.gla.document_service.entity.UserDocument;
//import com.gla.document_service.enums.DocumentType;
//import com.gla.document_service.repository.UserDocumentRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class DocumentService {
//
//    @Autowired
//    private Cloudinary cloudinary;
//
//    @Autowired
//    private UserDocumentRepository repository;
//
//    public UserDocument upload(String email, DocumentType type, MultipartFile file) {
//
//        try {
//            Map uploadResult = cloudinary.uploader().upload(
//                    file.getBytes(),
//                    ObjectUtils.emptyMap()
//            );
//
//            String url = uploadResult.get("secure_url").toString();
//
//            UserDocument doc = new UserDocument();
//            doc.setEmail(email);
//            doc.setDocumentType(type);
//            doc.setFileName(file.getOriginalFilename());
//            doc.setFileUrl(url);
//
//            return repository.save(doc);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Upload failed: " + e.getMessage());
//        }
//    }
//
//    public List<UserDocument> getByEmail(String email) {
//        return repository.findByEmail(email);
//    }
//}