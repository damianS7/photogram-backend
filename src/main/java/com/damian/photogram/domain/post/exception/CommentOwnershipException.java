package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CommentOwnershipException extends ApplicationException {
    private final Long commentId;
    private final Long customerId;

    public CommentOwnershipException(String message) {
        this(message, null, null);
    }

    public CommentOwnershipException(String message, Long commentId, Long customerId) {
        super(message);
        this.customerId = customerId;
        this.commentId = commentId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
