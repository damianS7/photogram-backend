package com.damian.photogram.domain.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CustomerEmailTakenException extends ApplicationException {
    public CustomerEmailTakenException(String message) {
        super(message);
    }
}
