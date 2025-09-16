package com.damian.photogram.domain.post.exception;

public class PostNotFoundException extends PostException {

    public PostNotFoundException(String message, Long postId) {
        super(message, postId);
    }

}
