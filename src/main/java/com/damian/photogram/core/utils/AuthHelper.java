package com.damian.photogram.core.utils;

import com.damian.photogram.app.user.User;
import com.damian.photogram.app.user.UserRole;
import com.damian.photogram.core.exception.PasswordMismatchException;
import com.damian.photogram.domain.customer.model.Customer;
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
        return AuthHelper.getLoggedUser().getCustomer();
    }

    public static User getLoggedUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static boolean isAdmin(Customer customer) {
        return customer.getRole().equals(UserRole.ADMIN);
    }

}
