package com.damian.photogram.domain.post.exception;

public class CommentException extends PostException {
    private final Long commentId;
    private final Long customerId;

    public CommentException(String message) {
        this(message, null, null, null);
    }

    public CommentException(String message, Long commentId, Long postId, Long customerId) {
        super(message, postId);
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
