package com.damian.photogram.common.utils;

import com.damian.photogram.common.exception.PasswordMismatchException;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthHelper {
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public static void validatePassword(Customer customer, String rawPassword) {
        if (!bCryptPasswordEncoder.matches(rawPassword, customer.getAuth().getPassword())) {
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
