package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageEmptyFileException extends ApplicationException {
    public ImageEmptyFileException(String message) {
        super(message);
    }
}
