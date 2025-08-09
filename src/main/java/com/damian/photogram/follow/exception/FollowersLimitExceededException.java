package com.damian.photogram.follow.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class FollowersLimitExceededException extends ApplicationException {
    public FollowersLimitExceededException(String message) {
        super(message);
    }
}
