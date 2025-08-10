package com.damian.photogram.customers;

import com.damian.photogram.auth.Auth;
import com.damian.photogram.customers.profile.Profile;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "customers")
public class Customer implements CustomerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL) // FetchType EAGER por defecto
    private Auth auth;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL) // FetchType EAGER por defecto
    private Profile profile;

    @Enumerated(EnumType.STRING)
    private CustomerRole role;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public Customer() {
        this.auth = new Auth(this);
        this.profile = new Profile(this);
        this.role = CustomerRole.CUSTOMER;
    }

    public Customer(Long id, String email, String password) {
        this();
        this.id = id;
        this.email = email;
        this.setPassword(password);
    }

    public Customer(String email, String password) {
        this(null, email, password);
    }

    public Auth getAuth() {
        return this.auth;
    }

    public void setAuth(Auth auth) {
        if (auth.getCustomer() != this) {
            auth.setCustomer(this);
        }
        this.auth = auth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomerRole getRole() {
        return role;
    }

    public void setRole(CustomerRole role) {
        this.role = role;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return auth.getPassword();
    }

    public void setPassword(String password) {
        this.auth.setPassword(password);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return CustomerDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return CustomerDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return CustomerDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return CustomerDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + this.role.name());
        return Collections.singletonList(authority);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        if (profile.getOwner() != this) {
            profile.setCustomer(this);
        }

        this.profile = profile;
    }

    public String getFullName() {
        return getProfile().getFirstName() + " " + getProfile().getLastName();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Customer{" +
               "id=" + id +
               ", email='" + email + '\'' +
               ", auth_id=" + auth.getId() +
               ", auth_password=" + auth.getPassword() +
               ", auth_account_status=" + auth.getAuthAccountStatus() +
               ", profile=" + profile.getId() +
               ", role=" + role +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';

    }
}
