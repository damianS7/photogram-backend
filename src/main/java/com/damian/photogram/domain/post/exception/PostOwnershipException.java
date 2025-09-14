package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class PostOwnershipException extends ApplicationException {
    public PostOwnershipException(String message) {
        super(message);
    }
}
