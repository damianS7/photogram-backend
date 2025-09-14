package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostImageTooLargeException extends ApplicationException {
    public PostImageTooLargeException(String message) {
        super(message);
    }
}
