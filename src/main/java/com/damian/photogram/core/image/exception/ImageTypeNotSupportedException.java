package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageTypeNotSupportedException extends ApplicationException {
    public ImageTypeNotSupportedException(String message) {
        super(message);
    }
}
