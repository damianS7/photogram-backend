package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageCompressionFailedException extends ApplicationException {
    public ImageCompressionFailedException(String message) {
        super(message);
    }
}
