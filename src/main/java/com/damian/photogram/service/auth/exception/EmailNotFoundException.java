package com.damian.photogram.service.auth.exception;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Thrown if an {@link UserDetailsService} implementation cannot
 * locate a {@link User} by its email.
 */
public class EmailNotFoundException extends UsernameNotFoundException {
    private final String email;

    public EmailNotFoundException(String message) {
        super(message);
        this.email = null;
    }

    public EmailNotFoundException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
