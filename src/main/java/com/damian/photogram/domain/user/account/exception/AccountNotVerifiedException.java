package com.damian.photogram.domain.user.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountNotVerifiedException extends ApplicationException {
    public AccountNotVerifiedException(String message) {
        super(message);
    }
}
