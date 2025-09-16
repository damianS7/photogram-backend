package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostException extends ApplicationException {
    private final Long postId;

    public PostException(String message, Long postId) {
        super(message);
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }
}
