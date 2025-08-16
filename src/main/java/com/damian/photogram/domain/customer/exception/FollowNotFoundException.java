package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowNotFoundException extends ApplicationException {
    public FollowNotFoundException(String message) {
        super(message);
    }
}
