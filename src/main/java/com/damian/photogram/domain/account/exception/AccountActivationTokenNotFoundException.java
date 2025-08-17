package com.damian.photogram.domain.account.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class AccountActivationTokenNotFoundException extends ApplicationException {
    public AccountActivationTokenNotFoundException(String message) {
        super(message);
    }
}
