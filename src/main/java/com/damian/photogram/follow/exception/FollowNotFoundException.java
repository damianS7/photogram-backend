package com.damian.photogram.follow.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class FollowNotFoundException extends ApplicationException {
    public FollowNotFoundException(String message) {
        super(message);
    }
}
