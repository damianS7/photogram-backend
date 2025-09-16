package com.damian.photogram.domain.post.exception;

public class PostAlreadyLikedException extends PostException {
    private final Long customerId;

    public PostAlreadyLikedException(String message, Long customerId, Long postId) {
        super(message, postId);
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
