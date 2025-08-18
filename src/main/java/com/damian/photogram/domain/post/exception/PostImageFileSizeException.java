package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostImageFileSizeException extends ApplicationException {
    public PostImageFileSizeException(String message) {
        super(message);
    }
}
