package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileUpdateValidationException extends ApplicationException {
    public ProfileUpdateValidationException(String message) {
        super(message);
    }
}
