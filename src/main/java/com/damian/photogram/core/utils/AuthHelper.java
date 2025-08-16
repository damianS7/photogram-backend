package com.damian.photogram.core.utils;

import com.damian.photogram.core.exception.PasswordMismatchException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthHelper {
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public static void validatePassword(Customer customer, String rawPassword) {
        if (!bCryptPasswordEncoder.matches(rawPassword, customer.getAccount().getPassword())) {
            throw new PasswordMismatchException(PasswordMismatchException.PASSWORD_MISMATCH);
        }
    }

    public static Customer getLoggedCustomer() {
        return (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static boolean isAdmin(Customer customer) {
        return customer.getRole().equals(CustomerRole.ADMIN);
    }

}
