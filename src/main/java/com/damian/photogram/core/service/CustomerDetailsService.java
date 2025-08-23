package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.security.CustomerDetails;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;

    public CustomerDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadCustomerByEmail(username);
    }

    public CustomerDetails loadCustomerByEmail(String email) throws UsernameNotFoundException {
        return customerRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new BadCredentialsException(
                                Exceptions.ACCOUNT.BAD_CREDENTIALS
                        )
                );
    }
}
