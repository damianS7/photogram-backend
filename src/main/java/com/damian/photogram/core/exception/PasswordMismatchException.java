package com.damian.photogram.core.exception;

public class PasswordMismatchException extends ApplicationException {
    public static final String PASSWORD_MISMATCH = "Password does not match.";

    public PasswordMismatchException(String message) {
        super(message);
    }
}
