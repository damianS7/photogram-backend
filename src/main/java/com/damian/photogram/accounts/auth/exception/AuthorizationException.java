package com.damian.photogram.accounts.auth.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class AuthorizationException extends ApplicationException {
    public AuthorizationException(String message) {
        super(message);
    }
}
