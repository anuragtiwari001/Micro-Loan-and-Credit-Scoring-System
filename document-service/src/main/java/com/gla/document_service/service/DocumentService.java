package com.gla.document_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gla.document_service.entity.UserDocument;
import com.gla.document_service.enums.DocumentType;
import com.gla.document_service.repository.UserDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserDocumentRepository repository;

    public UserDocument upload(String email, DocumentType type, MultipartFile file) {

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.emptyMap()
            );

            String url = uploadResult.get("secure_url").toString();

            UserDocument doc = new UserDocument();
            doc.setEmail(email);
            doc.setDocumentType(type);
            doc.setFileName(file.getOriginalFilename());
            doc.setFileUrl(url);

            return repository.save(doc);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    public List<UserDocument> getByEmail(String email) {
        return repository.findByEmail(email);
    }
}