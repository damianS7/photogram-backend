package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountActivationTokenExpiredException extends ApplicationException {
    public AccountActivationTokenExpiredException(String message) {
        super(message);
    }
}
