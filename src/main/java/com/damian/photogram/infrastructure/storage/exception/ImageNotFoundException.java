package com.damian.photogram.infrastructure.storage.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ImageNotFoundException extends ApplicationException {
    private final String path;
    private final String imageName;

    public ImageNotFoundException(String message, String path, String imageName) {
        super(message);
        this.path = path;
        this.imageName = imageName;
    }

    public ImageNotFoundException(String message) {
        this(message, null, null);
    }

    public String getImageName() {
        return imageName;
    }

    public String getPath() {
        return path;
    }
}
