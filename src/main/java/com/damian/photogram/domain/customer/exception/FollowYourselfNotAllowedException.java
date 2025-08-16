package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowYourselfNotAllowedException extends ApplicationException {
    public FollowYourselfNotAllowedException(String message) {
        super(message);
    }
}
