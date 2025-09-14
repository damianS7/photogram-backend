package com.damian.photogram.domain.user.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileNotOwnerException extends ApplicationException {
    public ProfileNotOwnerException(String message) {
        super(message);
    }
}
