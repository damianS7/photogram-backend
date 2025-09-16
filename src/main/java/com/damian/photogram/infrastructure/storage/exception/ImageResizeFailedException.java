package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageResizeFailedException extends ApplicationException {
    public ImageResizeFailedException(String message) {
        super(message);
    }
}
