package com.damian.photogram.accounts.token.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class TokenNotFoundException extends CustomerException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
