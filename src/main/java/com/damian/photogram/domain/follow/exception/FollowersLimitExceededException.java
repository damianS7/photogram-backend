package com.damian.photogram.domain.follow.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowersLimitExceededException extends ApplicationException {
    private final Long customerId;

    public FollowersLimitExceededException(String message, Long customerId) {
        super(message);
        this.customerId = customerId;
    }

    public FollowersLimitExceededException(String message) {
        this(message, null);
    }

    public Long getCustomerId() {
        return customerId;
    }
}
