package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountSuspendedException extends ApplicationException {
    public AccountSuspendedException(String message) {
        super(message);
    }
}
