package com.damian.photogram.domain.user.exception;

public class FollowAlreadyExistsException extends FollowException {

    public FollowAlreadyExistsException(String message, Long followerId, Long followedId) {
        super(message, followerId, followedId);
    }

}
