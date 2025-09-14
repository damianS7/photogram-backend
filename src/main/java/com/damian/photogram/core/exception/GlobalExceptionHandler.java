package com.damian.photogram.core.exception;

import com.damian.photogram.app.auth.exception.EmailNotFoundException;
import com.damian.photogram.core.common.ApiResponse;
import com.damian.photogram.domain.account.exception.*;
import com.damian.photogram.domain.customer.exception.*;
import com.damian.photogram.domain.post.exception.*;
import com.damian.photogram.domain.setting.exception.SettingNotFoundException;
import com.damian.photogram.domain.setting.exception.SettingNotOwnerException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

// todo review
@Order(99)
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ApiResponse.error(Exceptions.AUTH.BAD_CREDENTIALS));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(Exceptions.AUTH.ACCOUNT_SUSPENDED));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(Exceptions.AUTH.ACCOUNT_NOT_VERIFIED));
    }

    @ExceptionHandler(
            {
                    ProfileUpdateValidationException.class
            }
    ) // 400
    public ResponseEntity<ApiResponse<String>> handleBadRequest(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(MethodArgumentNotValidException ex) {
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
                    AccountNotVerifiedException.class,
                    AccountSuspendedException.class,
                    PasswordMismatchException.class,
                    FollowersLimitExceededException.class,
                    FollowYourselfNotAllowedException.class,
                    AccountVerificationTokenMismatchException.class,
                    AccountVerificationTokenUsedException.class,
                    PostNotAuthorException.class,
                    ProfileNotOwnerException.class,
                    SettingNotOwnerException.class
            }
    ) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    UsernameNotFoundException.class,
                    EmailNotFoundException.class,
                    EntityNotFoundException.class,
                    CustomerNotFoundException.class,
                    ProfileNotFoundException.class,
                    ProfilePhotoNotFoundException.class,
                    CommentNotFoundException.class,
                    LikeNotFoundException.class,
                    FollowNotFoundException.class,
                    SettingNotFoundException.class,
                    PostNotFoundException.class,
                    AccountNotFoundException.class,
                    AccountVerificationTokenNotFoundException.class
            }
    ) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    CustomerEmailTakenException.class,
                    FollowAlreadyExistsException.class,
                    PostAlreadyLikedException.class,
                    AccountVerificationNotPendingException.class
            }
    )
    // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }


    @ExceptionHandler(
            {
                    PostImageFileSizeExceededException.class,
                    MaxUploadSizeExceededException.class,
            }
    ) // 413 Payload Too Large
    public ResponseEntity<ApiResponse<String>> handleTooLarge(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE));
    }

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