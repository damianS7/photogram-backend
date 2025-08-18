package com.damian.photogram.core.exception;

import com.damian.photogram.app.auth.exception.AuthenticationBadCredentialsException;
import com.damian.photogram.app.auth.exception.AuthenticationException;
import com.damian.photogram.app.auth.exception.AuthorizationException;
import com.damian.photogram.app.auth.exception.JwtAuthenticationException;
import com.damian.photogram.core.utils.ApiResponse;
import com.damian.photogram.domain.account.exception.AccountDisabledException;
import com.damian.photogram.domain.account.exception.AccountNotFoundException;
import com.damian.photogram.domain.customer.exception.*;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

// FIXME code cleanup and add new exceptions
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation error", errors, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(
            {
                    AuthenticationException.class,
                    JwtAuthenticationException.class,
                    AuthenticationBadCredentialsException.class,
                    AccountDisabledException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(
            {
                    ImageNotFoundException.class,
                    EntityNotFoundException.class,
                    CustomerNotFoundException.class,
                    ProfileNotFoundException.class,
                    ProfilePhotoNotFoundException.class,
                    FollowNotFoundException.class,
                    PostNotFoundException.class,
                    AccountNotFoundException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    CustomerEmailTakenException.class,
                    FollowersLimitExceededException.class,
                    MaxUploadSizeExceededException.class,
                    FollowerAlreadyExistsException.class

            }
    )
    public ResponseEntity<ApiResponse<String>> handleConflitException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(
            {
                    AuthorizationException.class,
                    FollowerAuthorizationException.class,
                    ProfileAuthorizationException.class,
                    PasswordMismatchException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleAuthorizationException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    ApplicationException.class,
                    //                    RuntimeException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleApplicationException(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(
            {
                    Exception.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(
                                     "Unexpected error. " + ex.getMessage(),
                                     HttpStatus.INTERNAL_SERVER_ERROR
                             ));
    }


}