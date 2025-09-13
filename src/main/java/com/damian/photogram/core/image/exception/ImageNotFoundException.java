package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageNotFoundException extends ApplicationException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
