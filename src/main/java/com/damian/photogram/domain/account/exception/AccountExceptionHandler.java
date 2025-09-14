package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.common.ApiResponse;
import com.damian.photogram.core.exception.ApplicationException;
import com.damian.photogram.domain.customer.exception.CustomerEmailTakenException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// todo review
@Order(1)
@RestControllerAdvice
public class AccountExceptionHandler {
    @ExceptionHandler(
            {
                    AccountVerificationTokenExpiredException.class,
            }
    )
    public ResponseEntity<ApiResponse<String>> handleGone(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.GONE));
    }

    @ExceptionHandler(
            {
                    AccountVerificationNotPendingException.class,
                    CustomerEmailTakenException.class
            }
    )
    // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(
            {
                    AccountNotVerifiedException.class,
                    AccountSuspendedException.class,
                    AccountVerificationTokenMismatchException.class,
                    AccountVerificationTokenUsedException.class,
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(ApplicationException ex) {
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

}