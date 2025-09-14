package com.damian.photogram.domain.user.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CustomerNotFoundException extends ApplicationException {
    private final Long customerId;

    public CustomerNotFoundException(String message, Long customerId) {
        super(message);
        this.customerId = customerId;
    }

    public CustomerNotFoundException(String message) {
        this(message, null);
    }

    public Long getCustomerId() {
        return customerId;
    }
}
