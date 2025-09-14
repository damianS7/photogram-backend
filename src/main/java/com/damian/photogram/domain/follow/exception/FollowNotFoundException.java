package com.damian.photogram.domain.follow.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowNotFoundException extends ApplicationException {
    public FollowNotFoundException(String message) {
        super(message);
    }
}
