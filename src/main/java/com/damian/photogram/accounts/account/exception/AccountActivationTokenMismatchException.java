package com.damian.photogram.accounts.account.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class AccountActivationTokenMismatchException extends CustomerException {
    public AccountActivationTokenMismatchException(String message) {
        super(message);
    }
}
