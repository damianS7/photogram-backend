package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CommentNotAuthorException extends ApplicationException {
    public CommentNotAuthorException(String message) {
        super(message);
    }
}
