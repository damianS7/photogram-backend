package com.damian.photogram.posts.comments.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class CommentNotFoundException extends ApplicationException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
