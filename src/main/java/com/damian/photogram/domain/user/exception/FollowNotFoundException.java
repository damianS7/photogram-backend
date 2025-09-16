package com.damian.photogram.domain.user.exception;

public class FollowNotFoundException extends FollowException {
    public FollowNotFoundException(String message, Long followerId, Long followedId) {
        super(message, followerId, followedId);
    }
}
