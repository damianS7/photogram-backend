package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostImageFileSizeExceededException extends ApplicationException {
    public PostImageFileSizeExceededException(String message) {
        super(message);
    }
}
