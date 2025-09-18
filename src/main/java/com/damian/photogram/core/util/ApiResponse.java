package com.damian.photogram.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private T data;
    private Map<String, String> errors;
    private int status;

    public ApiResponse() {

    }

    public ApiResponse(String message, T data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(message, null, HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(null, data, HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(message, null, status.value());
    }

    public static <T> ApiResponse<T> error(String error, Map<String, String> errors, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(error, null, status.value());
        response.setErrors(errors);
        return response;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}