package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.common.ApiResponse;
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

    @ExceptionHandler(
            {
                    ImageEmptyFileException.class,
            }
    ) // 400
    public ResponseEntity<ApiResponse<String>> handleBadRequest(ApplicationException ex) {
        log.warn("Image upload failed: empty file.", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(
            {
                    ImageCompressionFailedException.class,
                    ImageResizeFailedException.class,
                    ImageFailedUploadException.class,
                    ImageFailedStorageException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleApplicationException(ApplicationException ex) {
        log.error("Image upload failed.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(Exceptions.IMAGE.UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(
            {
                    ImageNotFoundException.class,
            }
    ) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ApplicationException ex) {
        log.warn("Image not found.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    ImageTooLargeException.class,
            }
    ) // 413 Payload Too Large
    public ResponseEntity<ApiResponse<String>> handleTooLarge(RuntimeException ex) {
        log.warn("Image upload failed: file too large.", ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(
            {
                    ImageTypeNotSupportedException.class
            }
    ) // 415
    public ResponseEntity<ApiResponse<String>> invalidType(ApplicationException ex) {
        log.warn("Image upload failed: unsupported type.", ex);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                             .body(ApiResponse.error(
                                     ex.getMessage(),
                                     HttpStatus.UNSUPPORTED_MEDIA_TYPE
                             ));
    }
}