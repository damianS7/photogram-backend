package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageFailedStorageException extends ApplicationException {
    public ImageFailedStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageFailedStorageException(String message) {
        super(message);
    }
}
