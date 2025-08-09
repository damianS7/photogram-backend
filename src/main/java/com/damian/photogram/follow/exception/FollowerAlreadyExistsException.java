package com.damian.photogram.follow.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class FollowerAlreadyExistsException extends ApplicationException {
    public FollowerAlreadyExistsException(String message) {
        super(message);
    }
}
