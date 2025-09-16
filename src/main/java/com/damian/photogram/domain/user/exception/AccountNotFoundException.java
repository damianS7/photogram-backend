package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountNotFoundException extends ApplicationException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
