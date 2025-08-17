package com.damian.photogram.domain.post.exception;

import com.damian.photogram.app.auth.exception.AuthorizationException;

public class PostNotAuthorException extends AuthorizationException {
    public PostNotAuthorException(String message) {
        super(message);
    }
}
