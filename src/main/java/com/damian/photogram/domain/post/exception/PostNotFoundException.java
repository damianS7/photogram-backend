package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostNotFoundException extends ApplicationException {
    private final Long postId;

    public PostNotFoundException(String message, Long postId) {
        super(message);
        this.postId = postId;
    }

    public PostNotFoundException(String message) {
        this(message, null);
    }

    public Long getPostId() {
        return postId;
    }
}
