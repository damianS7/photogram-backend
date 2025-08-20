package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostNotAuthorException extends ApplicationException {
    public PostNotAuthorException(String message) {
        super(message);
    }
}
