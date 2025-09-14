package com.damian.photogram.app.auth.exception;

import com.damian.photogram.core.common.ApiResponse;
import com.damian.photogram.core.exception.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class AuthExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(
            {
                    UsernameNotFoundException.class,
                    EmailNotFoundException.class,
                    BadCredentialsException.class
            }
    )
    public ResponseEntity<?> handleBadCredentials(RuntimeException e) {
        log.warn("Failed login attempt. Bad credentials.", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(
                                     ApiResponse.error(Exceptions.AUTH.BAD_CREDENTIALS)
                             );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked(RuntimeException e) {
        log.warn("Failed login attempt. Account is suspended.", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(
                                     ApiResponse.error(Exceptions.AUTH.ACCOUNT_SUSPENDED)
                             );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(RuntimeException e) {
        log.warn("Failed login attempt. Account not verified.", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(
                                     ApiResponse.error(Exceptions.AUTH.ACCOUNT_NOT_VERIFIED)
                             );
    }
}