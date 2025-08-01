package com.damian.photogram.auth.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message);
    }
}
