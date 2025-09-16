package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowNotFoundException extends ApplicationException {
    private final Long followerId;
    private final Long followedId;

    public FollowNotFoundException(String message) {
        this(message, null, null);
    }

    public FollowNotFoundException(String message, Long followerId, Long followedId) {
        super(message);
        this.followedId = followedId;
        this.followerId = followerId;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public Long getFollowedId() {
        return followedId;
    }
}
