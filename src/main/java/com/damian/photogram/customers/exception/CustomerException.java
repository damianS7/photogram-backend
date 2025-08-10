package com.damian.photogram.customers.exception;

import com.damian.photogram.common.exception.ApplicationException;

public class CustomerException extends ApplicationException {
    public CustomerException(String message) {
        super(message);
    }
}
