package com.damian.photogram.app.auth.exception;

import com.damian.photogram.core.common.ApiResponse;
import com.damian.photogram.core.exception.Exceptions;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// todo review
@Order(1)
@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(
                                     ApiResponse.error(Exceptions.AUTH.BAD_CREDENTIALS)
                             );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(
                                     ApiResponse.error(Exceptions.AUTH.ACCOUNT_SUSPENDED)
                             );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(
                                     ApiResponse.error(Exceptions.AUTH.ACCOUNT_NOT_VERIFIED)
                             );
    }


}