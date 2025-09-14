package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileUpdateException extends ApplicationException {
    public ProfileUpdateException(String message) {
        super(message);
    }
}
