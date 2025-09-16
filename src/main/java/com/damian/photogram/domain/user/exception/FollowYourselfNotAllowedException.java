package com.damian.photogram.domain.user.exception;

public class FollowYourselfNotAllowedException extends FollowException {
    public FollowYourselfNotAllowedException(String message, Long followerId, Long followedId) {
        super(message, followerId, followedId);
    }
}
