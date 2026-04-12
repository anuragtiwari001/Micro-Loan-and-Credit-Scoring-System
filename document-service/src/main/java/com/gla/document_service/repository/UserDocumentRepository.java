package com.gla.document_service.repository;

import com.gla.document_service.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    List<UserDocument> findByEmail(String email);
}