package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowAlreadyExistsException extends ApplicationException {
    public FollowAlreadyExistsException(String message) {
        super(message);
    }
}
