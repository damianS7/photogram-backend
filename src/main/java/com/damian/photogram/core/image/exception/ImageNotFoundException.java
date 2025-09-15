package com.damian.photogram.core.image.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageNotFoundException extends ApplicationException {
    private final String imageName;

    public ImageNotFoundException(String message, String imageName) {
        super(message);
        this.imageName = imageName;
    }

    public ImageNotFoundException(String message) {
        this(message, null);
    }

    public String getImageName() {
        return imageName;
    }
}
