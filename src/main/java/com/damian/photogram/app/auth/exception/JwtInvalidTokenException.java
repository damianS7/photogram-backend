package com.damian.photogram.app.auth.exception;

import org.springframework.security.core.AuthenticationException;

// Remove and use ExpiredJwtException instead
public class JwtInvalidTokenException extends AuthenticationException {
    public JwtInvalidTokenException(String message) {
        super(message);
    }
}
