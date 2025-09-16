package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;
import com.damian.photogram.core.util.ApiResponse;
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

    @ExceptionHandler(FollowYourselfNotAllowedException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleFollowYourself(ApplicationException ex) {
        log.warn("Following yourself is not allowed.", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(FollowersLimitExceededException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleFollowerLimit(FollowersLimitExceededException ex) {
        log.warn(
                "customer: {} tried to follow customer: {} but reached max followers.",
                ex.getFollowerCustomerId(),
                ex.getFollowedCustomerId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(FollowNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleFollowNotFound(FollowNotFoundException ex) {
        log.debug(
                "Follow relationship between: follower: {} and followed: {} not found.",
                ex.getFollowerCustomerId(),
                ex.getFollowedCustomerId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(FollowBetweenUsersNotExistException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleFollowBetween(FollowBetweenUsersNotExistException ex) {
        log.debug(
                "customer: {} is not following customer: {}",
                ex.getFollowerCustomerId(),
                ex.getFollowedCustomerId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(FollowAlreadyExistsException.class)// Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleAlreadyFollowing(FollowAlreadyExistsException ex) {
        log.warn(
                "customer: {} already following customer: {}",
                ex.getFollowerCustomerId(),
                ex.getFollowedCustomerId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }
}