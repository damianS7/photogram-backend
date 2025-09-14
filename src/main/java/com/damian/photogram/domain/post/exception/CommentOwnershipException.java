package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CommentOwnershipException extends ApplicationException {
    public CommentOwnershipException(String message) {
        super(message);
    }
}
