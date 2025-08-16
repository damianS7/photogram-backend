package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowersLimitExceededException extends ApplicationException {
    public FollowersLimitExceededException(String message) {
        super(message);
    }
}
