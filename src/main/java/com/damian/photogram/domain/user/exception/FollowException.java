package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowException extends ApplicationException {
    private final Long followerCustomerId;
    private final Long followedCustomerId;

    public FollowException(String message, Long followerCustomerId, Long followedCustomerId) {
        super(message);
        this.followerCustomerId = followerCustomerId;
        this.followedCustomerId = followedCustomerId;
    }

    public Long getFollowedCustomerId() {
        return followedCustomerId;
    }

    public Long getFollowerCustomerId() {
        return followerCustomerId;
    }
}
