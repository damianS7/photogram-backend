package com.damian.photogram.core.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomerDetails extends UserDetails {
    String getEmail();

    void setEmail(String email);
}
