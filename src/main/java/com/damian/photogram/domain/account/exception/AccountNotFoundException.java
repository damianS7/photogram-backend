package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountNotFoundException extends ApplicationException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
