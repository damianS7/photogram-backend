package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class LikeNotFoundException extends ApplicationException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
