package com.damian.photogram.domain.post.exception;

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
public class PostExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(PostExceptionHandler.class);

    @ExceptionHandler(
            {
                    PostOwnershipException.class,
                    CommentOwnershipException.class
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(ApplicationException ex) {
        log.warn("Unauthorized attempt to access.", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(Exceptions.COMMON.NOT_OWNER, HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    PostNotFoundException.class,
                    LikeNotFoundException.class,
                    CommentNotFoundException.class,
            }
    ) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ApplicationException ex) {
        log.warn("Attempt to access invalid resources.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(Exceptions.COMMON.NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    PostAlreadyLikedException.class,
            }
    )
    // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(ApplicationException ex) {
        log.warn("Attempt to like a post twice.", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }


    @ExceptionHandler(
            {
                    PostImageTooLargeException.class,
            }
    ) // 413 Payload Too Large
    public ResponseEntity<ApiResponse<String>> handleTooLarge(RuntimeException ex) {
        log.warn("Attempt to upload a post image too large.", ex);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE));
    }
}