package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageFailedUploadException extends ApplicationException {
    public ImageFailedUploadException(String message) {
        super(message);
    }
}
