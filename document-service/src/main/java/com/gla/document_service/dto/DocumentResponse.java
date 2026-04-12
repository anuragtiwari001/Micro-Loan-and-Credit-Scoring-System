package com.gla.document_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {

    private Long id;
    private String documentType;
    private String fileUrl;
}