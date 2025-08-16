package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.app.auth.exception.AuthorizationException;

public class FollowerAuthorizationException extends AuthorizationException {
    public FollowerAuthorizationException(String message) {
        super(message);
    }
}
