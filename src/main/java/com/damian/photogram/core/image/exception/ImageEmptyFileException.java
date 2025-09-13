package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageEmptyFileException extends ApplicationException {
    public ImageEmptyFileException(String message) {
        super(message);
    }
}
