package com.damian.photogram.domain.post.exception;

public class PostOwnershipException extends PostException {
    private final Long customerId;

    public PostOwnershipException(String message, Long postId, Long customerId) {
        super(message, postId);
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
