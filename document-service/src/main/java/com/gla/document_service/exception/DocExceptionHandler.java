package com.gla.document_service.exception;

import com.gla.common_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class DocExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxSize(
            MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("File size exceeds maximum limit of 10MB", 400));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(
            RuntimeException ex) {

        String msg = ex.getMessage();

        if (msg != null && msg.contains("Access denied"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(msg, 403));

        if (msg != null && msg.contains("not found"))
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(msg, 404));

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(msg, 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal error: " + ex.getMessage(), 500));
    }
}