package com.damian.photogram.app.auth.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class AuthenticationBadCredentialsException extends BadCredentialsException {
    public AuthenticationBadCredentialsException(String message) {
        super(message);
    }
}
