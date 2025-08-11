package com.damian.photogram.posts.post.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class PostNotFoundException extends ApplicationException {
    public PostNotFoundException(String message) {
        super(message);
    }
}
