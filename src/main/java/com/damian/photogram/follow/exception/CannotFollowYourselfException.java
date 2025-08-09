package com.damian.photogram.follow.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class CannotFollowYourselfException extends ApplicationException {
    public CannotFollowYourselfException(String message) {
        super(message);
    }
}
