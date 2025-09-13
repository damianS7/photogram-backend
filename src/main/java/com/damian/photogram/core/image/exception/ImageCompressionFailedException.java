package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageCompressionFailedException extends ApplicationException {
    public ImageCompressionFailedException(String message) {
        super(message);
    }
}
