package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageTooLargeException extends ApplicationException {
    private final String size;

    public ImageTooLargeException(String message, String size) {
        super(message);
        this.size = size;
    }

    public ImageTooLargeException(String message, Long size) {
        this(message, size.toString());
    }

    public ImageTooLargeException(String message) {
        super(message);
        this.size = null;
    }

    public String getImageSize() {
        return size;
    }
}
