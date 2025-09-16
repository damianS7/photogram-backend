package com.damian.photogram.domain.post.exception;

public class CommentNotFoundException extends CommentException {
    public CommentNotFoundException(String message) {
        this(message, null, null, null);
    }

    public CommentNotFoundException(String message, Long commentId, Long postId, Long customerId) {
        super(message, commentId, postId, customerId);
    }
}
