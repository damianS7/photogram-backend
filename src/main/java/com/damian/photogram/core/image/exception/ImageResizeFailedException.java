package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageResizeFailedException extends ApplicationException {
    public ImageResizeFailedException(String message) {
        super(message);
    }
}
