package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Order(1)
@RestControllerAdvice
public class FileStorageExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(FileStorageExceptionHandler.class);

    @ExceptionHandler(FileStorageNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleFileNotFound(FileStorageNotFoundException ex) {
        log.error("File: {} not found in: {}", ex.getFileName(), ex.getPath(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(FileStorageFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleFileStorageFailed(FileStorageFailedException ex) {
        log.error("Failed to store file: {} at: {}", ex.getFileName(), ex.getPath(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class) // 413 Payload Too Large
    public ResponseEntity<ApiResponse<String>> handleTooLarge(RuntimeException ex) {
        log.warn("File upload too large: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                             .body(ApiResponse.error(
                                     "Uploaded file exceeds maximum allowed size.",
                                     HttpStatus.PAYLOAD_TOO_LARGE
                             ));
    }
}