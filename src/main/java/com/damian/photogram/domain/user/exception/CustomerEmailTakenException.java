package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CustomerEmailTakenException extends ApplicationException {
    private final String email;

    public CustomerEmailTakenException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
