package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageTooLargeException extends ApplicationException {
    public ImageTooLargeException(String message) {
        super(message);
    }
}
