package com.damian.photogram.accounts.account.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class AccountNotFoundException extends CustomerException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
