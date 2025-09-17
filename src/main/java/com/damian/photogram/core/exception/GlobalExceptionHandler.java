package com.damian.photogram.core.exception;

import com.damian.photogram.core.util.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Order(99)
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // o IllegalStateException
    @ExceptionHandler(IllegalStateException.class) // 500
    public ResponseEntity<ApiResponse<String>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error("Illegal state.", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // Logic service errors
    @ExceptionHandler(IllegalArgumentException.class) // 500
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal Argument exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error("Illegal argument.", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // 	No permissions.
    @ExceptionHandler(AccessDeniedException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ApiResponse.error("Access denied.", HttpStatus.UNAUTHORIZED));
    }

    // Media type not supported. Content-type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class) // 405
    public ResponseEntity<ApiResponse<String>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Media type not supported: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                             .body(ApiResponse.error("Media type not supported.", HttpStatus.UNSUPPORTED_MEDIA_TYPE));
    }

    // When you call POST and its not allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class) // 405
    public ResponseEntity<ApiResponse<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                             .body(ApiResponse.error("Method not supported.", HttpStatus.METHOD_NOT_ALLOWED));
    }

    // Invalid param type. ex Long instead string
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleMethodArgTypeMissmatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid param type: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error("Invalid param type.", HttpStatus.BAD_REQUEST));
    }

    // Missing path variable
    @ExceptionHandler(MissingPathVariableException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleMissingPathVariable(MissingPathVariableException ex) {
        log.warn("Missing path variable: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error("Missing path variable.", HttpStatus.BAD_REQUEST));
    }

    // missing param
    @ExceptionHandler(MissingServletRequestParameterException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing param error found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error("Missing param error.", HttpStatus.BAD_REQUEST));
    }

    // Invalid json / deserialization error
    @ExceptionHandler(HttpMessageNotReadableException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleSerializationError(HttpMessageNotReadableException ex) {
        log.warn("Serialization error found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error("Serialization error.", HttpStatus.BAD_REQUEST));
    }

    // Validation errors
    // @RequestParam @PathVariable fails
    @ExceptionHandler(ConstraintViolationException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleContraintViolation(ConstraintViolationException ex) {
        log.warn("Validation error found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error("Validation error.", HttpStatus.BAD_REQUEST));
    }

    // Conversion from Long to String for example
    @ExceptionHandler(BindException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleBindException(BindException ex) {
        log.warn("Validation error found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error("Validation error.", HttpStatus.BAD_REQUEST));
    }

    // For @Valid exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class) // 400
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {} errors -> {}", errors.size(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(
                                     Exceptions.COMMON.VALIDATION_FAILED,
                                     errors,
                                     HttpStatus.BAD_REQUEST
                             ));
    }

    // when you found and not exists in db
    @ExceptionHandler(EntityNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error("Entity not found", HttpStatus.NOT_FOUND));
    }

    // when you violates a db constraint
    @ExceptionHandler(DataIntegrityViolationException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Database tata integrity violation : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error("Integrity violation.", HttpStatus.CONFLICT));
    }

    // when @transaction fails
    @ExceptionHandler(TransactionSystemException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleIntegrityViolation(TransactionSystemException ex) {
        log.warn("Transaction failed : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error("Transaction failed.", HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class) // fallback
    public ResponseEntity<ApiResponse<String>> handleUnexpectedExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(
                                     "Unexpected internal server error.",
                                     HttpStatus.INTERNAL_SERVER_ERROR
                             ));
    }


}
