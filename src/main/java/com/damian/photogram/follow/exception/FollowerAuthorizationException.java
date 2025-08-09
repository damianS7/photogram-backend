package com.damian.photogram.follow.exception;

import com.damian.photogram.auth.exception.AuthorizationException;

public class FollowerAuthorizationException extends AuthorizationException {
    public FollowerAuthorizationException(String message) {
        super(message);
    }
}
