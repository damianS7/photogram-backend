package com.damian.photogram.accounts.account.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class AccountActivationTokenNotFoundException extends CustomerException {
    public AccountActivationTokenNotFoundException(String message) {
        super(message);
    }
}
