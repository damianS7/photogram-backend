package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfilePhotoNotFoundException extends ApplicationException {
    public ProfilePhotoNotFoundException(String message) {
        super(message);
    }
}
