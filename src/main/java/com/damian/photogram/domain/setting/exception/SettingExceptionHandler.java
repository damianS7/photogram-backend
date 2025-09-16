package com.damian.photogram.domain.setting.exception;

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
public class SettingExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(SettingExceptionHandler.class);

    @ExceptionHandler(SettingNotOwnerException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleAuthorization(SettingNotOwnerException ex) {
        log.warn("Attempt to access someone else settings. customerId: {}", ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(SettingNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleNotFound(SettingNotFoundException ex) {
        log.warn("Attempt to access non existing. settingId: {}", ex.getSettingId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }
}