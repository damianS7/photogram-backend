package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostAlreadyLikedException extends ApplicationException {
    public PostAlreadyLikedException(String message) {
        super(message);
    }
}
