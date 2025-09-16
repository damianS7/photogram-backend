package com.damian.photogram.domain.post.exception;

public class CommentOwnershipException extends CommentException {
    public CommentOwnershipException(String message) {
        this(message, null, null, null);
    }

    public CommentOwnershipException(String message, Long commentId, Long postId, Long customerId) {
        super(message, commentId, postId, customerId);
    }

}
