package com.damian.photogram.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private T data;
    private T errors;
    private HttpStatus status;

    public ApiResponse() {

    }

    public ApiResponse(String message, T data, HttpStatus status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(message, null, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(null, data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> success(T data, HttpStatus status) {
        return new ApiResponse<>(null, data, status);
    }

    public static <T> ApiResponse<T> success(String message, HttpStatus status) {
        return new ApiResponse<>(message, null, status);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(message, null, status);
    }

    public static <T> ApiResponse<T> error(String error, T errors, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(error, null, status);
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
        return this.status.value();
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public T getErrors() {
        return errors;
    }

    public void setErrors(T errors) {
        this.errors = errors;
    }
}