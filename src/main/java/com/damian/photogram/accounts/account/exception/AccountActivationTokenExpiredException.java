package com.damian.photogram.accounts.account.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class AccountActivationTokenExpiredException extends CustomerException {
    public AccountActivationTokenExpiredException(String message) {
        super(message);
    }
}
