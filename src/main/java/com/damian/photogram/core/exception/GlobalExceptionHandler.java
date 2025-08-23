package com.damian.photogram.core.exception;

import com.damian.photogram.app.auth.exception.AuthenticationException;
import com.damian.photogram.core.utils.ApiResponse;
import com.damian.photogram.domain.account.exception.*;
import com.damian.photogram.domain.customer.exception.*;
import com.damian.photogram.domain.post.exception.*;
import com.damian.photogram.domain.setting.exception.SettingNotFoundException;
import com.damian.photogram.domain.setting.exception.SettingNotOwnerException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            {
                    ImageEmptyFileException.class,
                    ProfileUpdateValidationException.class
            }
    )
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
                    AuthenticationException.class,
                    ExpiredJwtException.class,
                    BadCredentialsException.class,
                    AccountSuspendedException.class,
                    AccountNotVerifiedException.class,
            }
    )
    public ResponseEntity<ApiResponse<String>> handleUnauthorized(RuntimeException ex) {
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
                    CommentNotFoundException.class,
                    LikeNotFoundException.class,
                    FollowNotFoundException.class,
                    SettingNotFoundException.class,
                    PostNotFoundException.class,
                    AccountNotFoundException.class,
                    AccountActivationTokenNotFoundException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleNotFound(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(
            {
                    CustomerEmailTakenException.class,
                    FollowAlreadyExistsException.class,
                    PostAlreadyLikedException.class,
                    AccountActivationNotPendingException.class
            }
    )
    // Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handleConflit(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(
            {
                    PasswordMismatchException.class,
                    FollowersLimitExceededException.class,
                    FollowYourselfNotAllowedException.class,
                    AccountActivationTokenMismatchException.class,
                    PostNotAuthorException.class,
                    ProfileNotOwnerException.class,
                    SettingNotOwnerException.class
            }
    )
    public ResponseEntity<ApiResponse<String>> handleAuthorization(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(
            {
                    ImageFileSizeExceededException.class,
                    PostImageFileSizeExceededException.class,
                    MaxUploadSizeExceededException.class,
            }
    )
    public ResponseEntity<ApiResponse<String>> handleTooLarge(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(
            {
                    ImageInvalidException.class,
            }
    )
    public ResponseEntity<ApiResponse<String>> invalidType(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(
            {
                    AccountActivationTokenExpiredException.class,
            }
    )
    public ResponseEntity<ApiResponse<String>> handleGone(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.GONE));
    }


    @ExceptionHandler(
            {
                    ApplicationException.class,
                    ImageFailedUploadException.class
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