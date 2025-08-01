package com.damian.photogram.follower.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class FollowerAlreadyExistException extends ApplicationException {
    public FollowerAlreadyExistException(String message) {
        super(message);
    }
}
