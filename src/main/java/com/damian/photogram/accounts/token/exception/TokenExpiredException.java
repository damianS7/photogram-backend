package com.damian.photogram.accounts.token.exception;

import com.damian.photogram.customers.exception.CustomerException;

public class TokenExpiredException extends CustomerException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
