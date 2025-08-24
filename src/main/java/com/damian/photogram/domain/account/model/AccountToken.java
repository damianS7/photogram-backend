package com.damian.photogram.domain.account.model;

import com.damian.photogram.domain.account.enums.AccountTokenType;
import com.damian.photogram.domain.customer.model.Customer;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "customer_auth_tokens")
public class AccountToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column
    private String token;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountTokenType type;

    @Column
    private boolean used;

    @Column
    private Instant createdAt;

    @Column
    private Instant expiresAt;

    public AccountToken() {
        this.used = false;
        this.token = generateToken();
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);
    }

    public AccountToken(Customer customer) {
        this();
        this.customer = customer;
    }

    public static AccountToken create() {
        return new AccountToken();
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public AccountToken setCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public Long getId() {
        return id;
    }

    public AccountToken setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCustomerId() {
        return this.customer.getId();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public AccountToken setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public AccountToken setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public boolean isUsed() {
        return used;
    }

    public AccountToken setUsed(boolean used) {
        this.used = used;
        return this;
    }

    public AccountTokenType getType() {
        return type;
    }

    public AccountToken setType(AccountTokenType type) {
        this.type = type;
        return this;
    }

    public String getToken() {
        return token;
    }

    public AccountToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
