package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.util.ApiResponse;
import com.damian.photogram.core.exception.ApplicationException;
import com.damian.photogram.core.exception.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class ImageExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ImageExceptionHandler.class);

    @ExceptionHandler(ImageEmptyFileException.class) // 400
    public ResponseEntity<ApiResponse<String>> handleBadRequest(ApplicationException ex) {
        log.warn("Image file is empty.", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ImageCompressionFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleCompressionFailed(ImageCompressionFailedException ex) {
        log.error("Image compression failed.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(Exceptions.IMAGE.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(ImageResizeFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleResizeFailed(ImageResizeFailedException ex) {
        log.error("Image resize failed.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(Exceptions.IMAGE.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleUploadFailed(ImageUploadFailedException ex) {
        log.error("Image upload failed at: {} failed.", ex.getPath(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(
                                     Exceptions.IMAGE.UPLOAD_FAILED,
                                     HttpStatus.INTERNAL_SERVER_ERROR
                             ));
    }

    @ExceptionHandler(ImageStorageFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleStorageFailed(ImageStorageFailedException ex) {
        log.error("Image storage failed at: {} failed.", ex.getPath(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(
                                     Exceptions.IMAGE.UPLOAD_FAILED,
                                     HttpStatus.INTERNAL_SERVER_ERROR
                             ));
    }

    @ExceptionHandler(ImageNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ImageNotFoundException ex) {
        log.warn("Image: {} not found in: {}", ex.getImageName(), ex.getPath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ImageTooLargeException.class) // 413 Payload Too Large
    public ResponseEntity<ApiResponse<String>> handleTooLarge(ImageTooLargeException ex) {
        log.warn("Image size: {} is too large.", ex.getImageSize(), ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(ImageTypeNotSupportedException.class) // 415
    public ResponseEntity<ApiResponse<String>> invalidType(ImageTypeNotSupportedException ex) {
        log.warn("Image type: {} not supported.", ex.getImageType(), ex);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                             .body(ApiResponse.error(
                                     ex.getMessage(),
                                     HttpStatus.UNSUPPORTED_MEDIA_TYPE
                             ));
    }
}