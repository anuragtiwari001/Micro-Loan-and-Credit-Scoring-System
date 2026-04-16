package com.gla.document_service.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.document_service.dto.DocumentResponse;
import com.gla.document_service.enums.DocumentType;
import com.gla.document_service.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Document upload and retrieval endpoints for document-service (:8086).
 *
 * POST /api/v1/docs/upload            → USER uploads a KYC document
 * GET  /api/v1/docs/admin/{email}     → ADMIN fetches all documents for a user
 *
 * FILES:
 *   Accepted types: PDF, JPG, PNG (validated in DocumentService)
 *   Max size:       10MB (set in application.properties)
 *   Storage:        Cloudinary (CDN) — returns secure HTTPS URL
 *   Metadata:       stored in gla_documents.user_document
 *
 * AUTHENTICATION:
 *   Both endpoints require a valid JWT (enforced by SecurityConfig).
 *   Upload: email taken from JWT (user cannot upload for another user).
 *   Admin fetch: ADMIN role required.
 */
@RestController
@RequestMapping("/api/v1/docs")
public class DocumentController {

    @Autowired
    private DocumentService service;

    // ─────────────────────────────────────────────────────────────────────────
    // Upload
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Uploads a KYC document to Cloudinary and stores the URL in the database.
     *
     * Multipart form fields:
     *   file → binary file (PDF / JPG / PNG, max 10MB)
     *   type → PAN | AADHAAR | SALARY_SLIP | BANK_STATEMENT
     *
     * Response: 201 { success:true, data: DocumentResponse { id, documentType, fileUrl } }
     *
     * NOTE: Returns DocumentResponse (not UserDocument entity) to hide
     *       internal fields like the email and Cloudinary public_id.
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type,
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        DocumentResponse response = service.upload(email, type, file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Document uploaded successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin: fetch all documents for a user
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all uploaded documents for the given email address.
     * Called by loan-service.getUserDocuments() when building the admin dashboard.
     *
     * ADMIN role required.
     *
     * Response: 200 { success:true, data: [ DocumentResponse, ... ] }
     */
    @GetMapping("/admin/{email}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocs(
            @PathVariable String email,
            HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: ADMIN role required");
        }

        List<DocumentResponse> docs = service.getByEmail(email);
        return ResponseEntity.ok(
                ApiResponse.success("Documents retrieved", docs));
    }
}




//package com.gla.document_service.controller;
//
//import com.gla.document_service.dto.DocumentResponse;
//import com.gla.document_service.entity.UserDocument;
//import com.gla.document_service.enums.DocumentType;
//import com.gla.document_service.service.DocumentService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/v1/docs")
//public class DocumentController {
//
//    @Autowired
//    private DocumentService service;
//
//    // 🔥 USER UPLOAD
//    @PostMapping("/upload")
//    public UserDocument upload(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("type") DocumentType type,
//            HttpServletRequest request
//    ) {
//
//        String email = (String) request.getAttribute("email");
//
//        return service.upload(email, type, file);
//    }
//
//    // 🔥 ADMIN FETCH
//    @GetMapping("/admin/{email}")
//    public List<DocumentResponse> getDocs(
//            @PathVariable String email,
//            HttpServletRequest request
//    ) {
//
//        String role = (String) request.getAttribute("role");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access denied");
//        }
//
//        return service.getByEmail(email)
//                .stream()
//                .map(doc -> new DocumentResponse(
//                        doc.getId(),
//                        doc.getDocumentType().name(),
//                        doc.getFileUrl()
//                ))
//                .collect(Collectors.toList());
//    }
//}