package com.gla.document_service.controller;

import com.gla.document_service.dto.DocumentResponse;
import com.gla.document_service.entity.UserDocument;
import com.gla.document_service.enums.DocumentType;
import com.gla.document_service.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/docs")
public class DocumentController {

    @Autowired
    private DocumentService service;

    // 🔥 USER UPLOAD
    @PostMapping("/upload")
    public UserDocument upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type,
            HttpServletRequest request
    ) {

        String email = (String) request.getAttribute("email");

        return service.upload(email, type, file);
    }

    // 🔥 ADMIN FETCH
    @GetMapping("/admin/{email}")
    public List<DocumentResponse> getDocs(
            @PathVariable String email,
            HttpServletRequest request
    ) {

        String role = (String) request.getAttribute("role");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access denied");
        }

        return service.getByEmail(email)
                .stream()
                .map(doc -> new DocumentResponse(
                        doc.getId(),
                        doc.getDocumentType().name(),
                        doc.getFileUrl()
                ))
                .collect(Collectors.toList());
    }
}