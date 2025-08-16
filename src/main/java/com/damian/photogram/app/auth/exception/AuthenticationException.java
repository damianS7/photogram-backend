package com.damian.photogram.app.auth.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message);
    }
}
