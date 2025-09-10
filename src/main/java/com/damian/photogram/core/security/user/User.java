package com.damian.photogram.core.security.user;

import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.account.model.Account;
import com.damian.photogram.domain.customer.enums.UserRole;
import com.damian.photogram.domain.customer.model.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class User implements UserDetails {
    private final Customer user;

    public User(Customer user) {
        this.user = user;
    }

    public Customer getCustomer() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public UserRole getRole() {
        return user.getRole();
    }

    public Account getAccount() {
        return user.getAccount();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getAccount().getAccountStatus().equals(AccountStatus.SUSPENDED);
    }

    @Override
    public boolean isEnabled() {
        return user.getAccount().getAccountStatus().equals(AccountStatus.VERIFIED);
    }
}
