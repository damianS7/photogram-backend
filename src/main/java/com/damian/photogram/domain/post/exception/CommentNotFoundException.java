package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CommentNotFoundException extends ApplicationException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
