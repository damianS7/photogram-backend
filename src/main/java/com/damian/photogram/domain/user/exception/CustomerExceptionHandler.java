package com.damian.photogram.domain.user.exception;

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
public class CustomerExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomerExceptionHandler.class);

    @ExceptionHandler(ProfileNotOwnerException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleProfileOwnership(ProfileNotOwnerException ex) {
        log.warn(
                "Unauthorized attempt by customer: {} to access profile: {}",
                ex.getCustomerId(),
                ex.getProfileId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(CustomerNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.warn("Failed to find a customer: {}", ex.getCustomerId(), ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProfileNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleProfileNotFound(ProfileNotFoundException ex) {
        log.warn(
                "Failed to find a profile: {} or profile from customer: {}",
                ex.getProfileId(),
                ex.getCustomerId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProfileImageNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleProfileImageNotFound(ProfileImageNotFoundException ex) {
        log.warn("Failed to find profile: {} image.", ex.getProfileId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProfileUpdateException.class) // 400
    public ResponseEntity<ApiResponse<String>> handleProfileUpdate(ProfileUpdateException ex) {
        log.warn("Failed to update profile: {} for customer: {}", ex.getProfileId(), ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CustomerEmailTakenException.class) // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleEmailAlreadyTaken(CustomerEmailTakenException ex) {
        log.warn("Attempt to use an email: {} that is already taken.", ex.getEmail(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }
}