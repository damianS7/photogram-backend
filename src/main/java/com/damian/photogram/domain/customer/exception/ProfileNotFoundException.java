package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileNotFoundException extends ApplicationException {
    public ProfileNotFoundException(String message) {
        super(message);
    }
}
