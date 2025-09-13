package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageFileSizeExceededException extends ApplicationException {
    public ImageFileSizeExceededException(String message) {
        super(message);
    }
}
