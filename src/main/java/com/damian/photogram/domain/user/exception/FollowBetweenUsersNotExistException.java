package com.damian.photogram.domain.user.exception;

public class FollowBetweenUsersNotExistException extends FollowException {
    public FollowBetweenUsersNotExistException(String message, Long followerId, Long followedId) {
        super(message, followerId, followedId);
    }
}
