package com.damian.photogram.domain.follow.exception;

import com.damian.photogram.core.common.ApiResponse;
import com.damian.photogram.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class FollowExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(FollowExceptionHandler.class);

    @ExceptionHandler(
            {
                    FollowersLimitExceededException.class,
                    FollowYourselfNotAllowedException.class,
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(ApplicationException ex) {
        log.warn("Follow operation not allowed.", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    FollowNotFoundException.class,
            }
    ) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ApplicationException ex) {
        log.warn("Follow not found.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    FollowAlreadyExistsException.class,
            }
    )// Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(ApplicationException ex) {
        log.warn("Attempt to follow same user.", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }
}