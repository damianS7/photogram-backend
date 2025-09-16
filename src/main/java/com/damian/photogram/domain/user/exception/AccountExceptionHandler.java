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
public class AccountExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AccountExceptionHandler.class);

    @ExceptionHandler(AccountVerificationTokenExpiredException.class) // 410
    public ResponseEntity<ApiResponse<String>> handleAccountVerificationTokenExpired(
            AccountVerificationTokenExpiredException ex
    ) {
        log.warn(
                "Customer: {} account: {} verification token: {} is expired.",
                ex.getCustomerId(),
                ex.getAccountId(),
                ex.getToken()
        );
        return ResponseEntity.status(HttpStatus.GONE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.GONE));
    }

    @ExceptionHandler(AccountVerificationNotPendingException.class) // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleAccountVerificationNotPending(
            AccountVerificationNotPendingException ex
    ) {
        log.warn(
                "Customer: {} account: {} verification is not pending for verification.",
                ex.getCustomerId(),
                ex.getAccountId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(AccountVerificationTokenUsedException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleAccountVerificationTokenUsed(AccountVerificationTokenUsedException ex) {
        log.warn(
                "Customer: {} account: {} verification token: {} is used.",
                ex.getCustomerId(),
                ex.getAccountId(),
                ex.getToken()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(AccountInvalidPasswordConfirmationException.class) // 403
    public ResponseEntity<ApiResponse<String>> handlAccountInvalidPasswordConfirmation(
            AccountInvalidPasswordConfirmationException ex
    ) {
        log.debug("Customer: {} account: {} password confirmation failed.", ex.getCustomerId(), ex.getAccountId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(AccountVerificationTokenNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleAccountVerificationTokenNotFound(
            AccountVerificationTokenNotFoundException ex
    ) {
        log.warn(
                "Customer: {} account: {} verification token: {} not found.",
                ex.getCustomerId(),
                ex.getAccountId(),
                ex.getToken()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(AccountNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn(
                "Customer: {} account: {} not found.",
                ex.getCustomerId(),
                ex.getAccountId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }
}