package com.damian.photogram.domain.user.exception;

public class CustomerNotFoundException extends CustomerException {
    public CustomerNotFoundException(String message, String username) {
        super(message, username);
    }

    public CustomerNotFoundException(String message, Long customerId) {
        super(message, customerId);
    }

}
