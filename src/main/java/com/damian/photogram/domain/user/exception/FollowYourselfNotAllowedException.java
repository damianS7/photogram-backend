package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowYourselfNotAllowedException extends ApplicationException {
    public FollowYourselfNotAllowedException(String message) {
        super(message);
    }
}
