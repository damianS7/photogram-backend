package com.damian.photogram.app.auth.exception;

import org.springframework.security.core.AuthenticationException;

// Remove and use ExpiredJwtException instead
public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String message) {
        super(message);
    }
}
