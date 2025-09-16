package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageStorageFailedException extends ApplicationException {
    private final String path;

    public ImageStorageFailedException(String message, String path) {
        super(message);
        this.path = path;
    }

    public ImageStorageFailedException(String message) {
        this(message, null);
    }

    public String getPath() {
        return path;
    }
}
