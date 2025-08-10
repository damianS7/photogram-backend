package com.damian.photogram.customers;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomerDetails extends UserDetails {
    String getEmail();

    void setEmail(String email);
}
