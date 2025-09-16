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
public class AccountExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AccountExceptionHandler.class);

    @ExceptionHandler(
            {
                    AccountVerificationTokenExpiredException.class,
            }
    ) // 410
    public ResponseEntity<ApiResponse<String>> handleGone(ApplicationException ex) {
        log.warn("Attempt to verify account with expired token.", ex);
        return ResponseEntity.status(HttpStatus.GONE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.GONE));
    }

    @ExceptionHandler(
            {
                    AccountVerificationNotPendingException.class,

            }
    ) // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(ApplicationException ex) {
        log.warn("Attempt to verify account not awaiting verification.", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(
            {
                    AccountVerificationTokenUsedException.class,
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(AccountVerificationTokenUsedException ex) {
        log.error("Failed to verify account due to used token for customerId: {}", ex.getCustomerId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    AccountInvalidPasswordConfirmationException.class
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(AccountInvalidPasswordConfirmationException ex) {
        log.debug("Failed to validate password for customerId: {}", ex.getCustomerId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    AccountNotFoundException.class,
                    AccountVerificationTokenNotFoundException.class
            }
    ) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ApplicationException ex) {
        log.warn("Account resource not found.", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }
}