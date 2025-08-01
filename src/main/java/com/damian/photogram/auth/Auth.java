package com.damian.photogram.auth;

import com.damian.photogram.customer.Customer;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_auth")
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column(name = "email_verification_status")
    @Enumerated(EnumType.STRING)
    private AuthEmailVerificationStatus emailVerificationStatus;

    @Column(name = "auth_account_status")
    @Enumerated(EnumType.STRING)
    private AuthAccountStatus authAccountStatus;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column
    private Instant updatedAt;

    public Auth() {
        this.authAccountStatus = AuthAccountStatus.ENABLED;
        this.emailVerificationStatus = AuthEmailVerificationStatus.NOT_VERIFIED;
    }

    public Auth(Customer customer) {
        this();
        this.customer = customer;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return this.customer.getId();
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public boolean isEmailVerified() {
        return this.emailVerificationStatus.equals(AuthEmailVerificationStatus.VERIFIED);
    }

    public AuthAccountStatus getAuthAccountStatus() {
        return this.authAccountStatus;
    }

    public void setAuthAccountStatus(AuthAccountStatus authAccountStatus) {
        this.authAccountStatus = authAccountStatus;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
