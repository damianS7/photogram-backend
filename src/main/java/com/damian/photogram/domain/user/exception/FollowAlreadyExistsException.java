package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowAlreadyExistsException extends ApplicationException {
    private final Long followerId;
    private final Long followedId;

    public FollowAlreadyExistsException(String message, Long followerId, Long followedId) {
        super(message);
        this.followedId = followedId;
        this.followerId = followerId;
    }

    public FollowAlreadyExistsException(String message) {
        this(message, null, null);
    }

    public Long getFollowerId() {
        return followerId;
    }

    public Long getFollowedId() {
        return followedId;
    }
}
