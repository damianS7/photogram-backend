package com.damian.photogram.follower.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class MaxFollowersLimitReachedException extends ApplicationException {
    public MaxFollowersLimitReachedException(String message) {
        super(message);
    }
}
