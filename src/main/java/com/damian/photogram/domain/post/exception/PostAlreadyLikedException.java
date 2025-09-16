package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostAlreadyLikedException extends ApplicationException {
    private final Long customerId;
    private final Long postId;

    public PostAlreadyLikedException(String message) {
        this(message, null, null);
    }

    public PostAlreadyLikedException(String message, Long customerId, Long postId) {
        super(message);
        this.postId = postId;
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getPostId() {
        return postId;
    }
}
