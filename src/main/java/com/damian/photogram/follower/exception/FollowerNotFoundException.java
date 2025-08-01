package com.damian.photogram.follower.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class FollowerNotFoundException extends ApplicationException {
    public FollowerNotFoundException(String message) {
        super(message);
    }
}
