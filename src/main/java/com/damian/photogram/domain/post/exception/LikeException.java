package com.damian.photogram.domain.post.exception;

public class LikeException extends PostException {
    private final Long likeId;
    private final Long customerId;

    public LikeException(String message, Long likeId, Long postId, Long customerId) {
        super(message, postId);
        this.likeId = likeId;
        this.customerId = customerId;
    }

    public Long getLikeId() {
        return likeId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
