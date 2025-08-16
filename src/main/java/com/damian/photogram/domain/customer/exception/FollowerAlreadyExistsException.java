package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class FollowerAlreadyExistsException extends ApplicationException {
    public FollowerAlreadyExistsException(String message) {
        super(message);
    }
}
