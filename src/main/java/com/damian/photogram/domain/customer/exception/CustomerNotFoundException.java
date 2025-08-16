package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CustomerNotFoundException extends ApplicationException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
