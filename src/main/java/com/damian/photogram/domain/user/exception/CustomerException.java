package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class CustomerException extends ApplicationException {
    private final Long customerId;
    private final String username;

    public CustomerException(String message, String username) {
        super(message);
        this.username = username;
        this.customerId = null;
    }

    public CustomerException(String message, Long customerId) {
        super(message);
        this.customerId = customerId;
        this.username = null;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getUsername() {
        return username;
    }
}
