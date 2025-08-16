package com.damian.photogram.app.auth.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AuthorizationException extends ApplicationException {
    public AuthorizationException(String message) {
        super(message);
    }
}
