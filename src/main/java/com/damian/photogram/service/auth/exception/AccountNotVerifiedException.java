package com.damian.photogram.service.auth.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountNotVerifiedException extends ApplicationException {
    public AccountNotVerifiedException(String message) {
        super(message);
    }
}
