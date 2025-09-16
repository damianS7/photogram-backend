package com.damian.photogram.domain.user.exception;

public class FollowersLimitExceededException extends FollowException {

    public FollowersLimitExceededException(String message, Long followerId, Long followedId) {
        super(message, followerId, followedId);
    }
}
