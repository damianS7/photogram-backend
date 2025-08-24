package com.damian.photogram.domain.account.model;

import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.model.Customer;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_auth")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column(name = "account_status")
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column
    private Instant updatedAt;

    public Account() {
        this.accountStatus = AccountStatus.PENDING_VERIFICATION;
    }

    public Account(Customer customer) {
        this();
        this.customer = customer;
    }

    public static Account create() {
        return new Account();
    }

    public Customer getOwner() {
        return this.customer;
    }

    public Account setOwner(Customer customer) {
        this.customer = customer;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Account setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCustomerId() {
        return this.customer.getId();
    }

    public String getPassword() {
        return passwordHash;
    }

    public Account setPassword(String password) {
        this.passwordHash = password;
        return this;
    }

    public boolean isEmailVerified() {
        return this.accountStatus.equals(AccountStatus.ACTIVE);
    }

    public AccountStatus getAccountStatus() {
        return this.accountStatus;
    }

    public Account setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Account setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
