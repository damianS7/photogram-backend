package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostOwnershipException extends ApplicationException {
    private final Long postId;
    private final Long customerId;

    public PostOwnershipException(String message) {
        this(message, null, null);
    }

    public PostOwnershipException(String message, Long postId, Long customerId) {
        super(message);
        this.customerId = customerId;
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
