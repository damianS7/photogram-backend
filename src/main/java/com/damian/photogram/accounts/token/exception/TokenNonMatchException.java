package com.damian.photogram.accounts.token.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class TokenNonMatchException extends CustomerException {
    public TokenNonMatchException(String message) {
        super(message);
    }
}
