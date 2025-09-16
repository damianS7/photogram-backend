package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CustomerNotFoundException extends ApplicationException {
    private final Long customerId;
    private final String username;

    public CustomerNotFoundException(String message, String username) {
        super(message);
        this.username = username;
        this.customerId = null;
    }

    public CustomerNotFoundException(String message, Long customerId) {
        super(message);
        this.customerId = customerId;
        this.username = null;
    }

    public CustomerNotFoundException(String message) {
        this(message, "");
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getUsername() {
        return username;
    }
}
