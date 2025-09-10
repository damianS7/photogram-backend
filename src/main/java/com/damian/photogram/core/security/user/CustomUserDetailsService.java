package com.damian.photogram.core.security.user;

import com.damian.photogram.app.auth.exception.EmailNotFoundException;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByEmail(username);
    }

    public UserDetails loadUserByEmail(String email) throws EmailNotFoundException {
        Customer customer = customerRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new EmailNotFoundException(
                                Exceptions.AUTH.BAD_CREDENTIALS
                        )
                );

        return new User(customer);
    }
}
