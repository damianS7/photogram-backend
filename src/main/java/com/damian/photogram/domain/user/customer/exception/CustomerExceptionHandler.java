package com.damian.photogram.domain.user.customer.exception;

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
public class CustomerExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomerExceptionHandler.class);

    @ExceptionHandler(
            {
                    ProfileNotOwnerException.class,
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(ApplicationException ex) {
        log.warn("Unauthorized attempt to access.", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(CustomerNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(CustomerNotFoundException ex) {
        log.warn("Failed to find a Customer with id: {}", ex.getCustomerId(), ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProfileNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ProfileNotFoundException ex) {
        log.warn(
                "Failed to find a profile with id: {} or profile from customer with id: {}",
                ex.getProfileId(),
                ex.getCustomerId(),
                ex
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProfileImageNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ProfileImageNotFoundException ex) {
        log.warn("Failed to find profile image for profileId: {}", ex.getProfileId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProfileUpdateException.class) // 400
    public ResponseEntity<ApiResponse<String>> handleBadRequest(ProfileUpdateException ex) {
        log.warn("Failed to update profileId: {} for customerId: {}", ex.getProfileId(), ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(CustomerEmailTakenException.class) // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(CustomerEmailTakenException ex) {
        log.warn("Attempt to use an email that is already taken: {}", ex.getEmail(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }
}