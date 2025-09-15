package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageTypeNotSupportedException extends ApplicationException {
    private final String type;

    public ImageTypeNotSupportedException(String message, String type) {
        super(message);
        this.type = type;
    }

    public ImageTypeNotSupportedException(String message) {
        this(message, null);
    }

    public String getImageType() {
        return type;
    }
}
