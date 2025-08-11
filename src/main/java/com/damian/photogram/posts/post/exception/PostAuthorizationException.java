package com.damian.photogram.posts.post.exception;

import com.damian.photogram.auth.exception.AuthorizationException;

public class PostAuthorizationException extends AuthorizationException {
    public PostAuthorizationException(String message) {
        super(message);
    }
}
