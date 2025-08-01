package com.damian.photogram.auth.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class AuthenticationBadCredentialsException extends BadCredentialsException {
    public AuthenticationBadCredentialsException(String message) {
        super(message);
    }
}
