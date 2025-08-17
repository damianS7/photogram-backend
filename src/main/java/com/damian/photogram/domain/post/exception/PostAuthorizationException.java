package com.damian.photogram.domain.post.exception;

import com.damian.photogram.app.auth.exception.AuthorizationException;

public class PostAuthorizationException extends AuthorizationException {
    public PostAuthorizationException(String message) {
        super(message);
    }
}
