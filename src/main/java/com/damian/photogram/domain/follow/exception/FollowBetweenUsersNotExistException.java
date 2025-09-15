package com.damian.photogram.domain.follow.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowBetweenUsersNotExistException extends ApplicationException {
    private final Long followerId;
    private final Long followedId;

    public FollowBetweenUsersNotExistException(String message) {
        this(message, null, null);
    }

    public FollowBetweenUsersNotExistException(String message, Long followerId, Long followedId) {
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
