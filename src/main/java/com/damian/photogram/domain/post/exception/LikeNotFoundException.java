package com.damian.photogram.domain.post.exception;

public class LikeNotFoundException extends LikeException {

    public LikeNotFoundException(String message, Long likeId, Long postId, Long customerId) {
        super(message, likeId, postId, customerId);
    }

    public LikeNotFoundException(String message) {
        super(message, null, null, null);
    }

}
