package com.damian.photogram.app.auth.exception;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Thrown if an {@link UserDetailsService} implementation cannot locate a {@link User} by
 * its email.
 */
public class EmailNotFoundException extends UsernameNotFoundException {
    public EmailNotFoundException(String message) {
        super(message);
    }
}
