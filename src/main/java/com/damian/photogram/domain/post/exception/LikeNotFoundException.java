package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class LikeNotFoundException extends ApplicationException {
    private final Long postId;
    private final Long notLikedByCustomerId;

    public LikeNotFoundException(String message) {
        this(message, null);
    }

    public LikeNotFoundException(String message, Long postId) {
        this(message, postId, null);
    }

    public LikeNotFoundException(String message, Long postId, Long notLikedByCustomerId) {
        super(message);
        this.postId = postId;
        this.notLikedByCustomerId = notLikedByCustomerId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getNotLikedByCustomerId() {
        return notLikedByCustomerId;
    }
}
