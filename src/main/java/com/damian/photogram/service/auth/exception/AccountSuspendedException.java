package com.damian.photogram.service.auth.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountSuspendedException extends ApplicationException {
    public AccountSuspendedException(String message) {
        super(message);
    }
}
