package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostNotFoundException extends ApplicationException {
    public PostNotFoundException(String message) {
        super(message);
    }
}
