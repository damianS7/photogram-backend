package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CommentNotFoundException extends ApplicationException {
    private final Long commentId;

    public CommentNotFoundException(String message) {
        this(message, null);
    }

    public CommentNotFoundException(String message, Long commentId) {
        super(message);
        this.commentId = commentId;
    }

    public Long getCommentId() {
        return commentId;
    }
}
