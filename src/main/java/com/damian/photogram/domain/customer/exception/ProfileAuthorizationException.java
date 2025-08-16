package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.app.auth.exception.AuthorizationException;

public class ProfileAuthorizationException extends AuthorizationException {
    public ProfileAuthorizationException(String message) {
        super(message);
    }
}
