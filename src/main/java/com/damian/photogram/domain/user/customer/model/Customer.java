package com.damian.photogram.domain.user.customer.model;

import com.damian.photogram.domain.user.customer.enums.UserRole;
import com.damian.photogram.domain.user.account.model.Account;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.function.Consumer;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public Customer() {
        this.account = new Account(this);
        this.profile = new Profile(this);
        this.role = UserRole.CUSTOMER;
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

    public static Customer create() {
        return new Customer();
    }

    public Account getAccount() {
        return this.account;
    }

    public Customer setAccount(Account account) {
        if (account.getOwner() != this) {
            account.setOwner(this);
        }
        this.account = account;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Customer setId(Long id) {
        this.id = id;
        return this;
    }

    public UserRole getRole() {
        return role;
    }

    public Customer setRole(UserRole role) {
        this.role = role;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Customer setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return account.getPassword();
    }

    public Customer setPassword(String password) {
        this.account.setPassword(password);
        return this;
    }

    public String getUsername() {
        return this.profile.getUsername();
    }

    public Profile getProfile() {
        return profile;
    }

    public Customer setProfile(Profile profile) {
        if (profile.getOwner() != this) {
            profile.setOwner(this);
        }

        this.profile = profile;
        return this;
    }

    public Customer setProfile(Consumer<Profile> profileInitializer) {
        if (this.profile == null) {
            this.profile = new Profile();
        }

        profileInitializer.accept(this.profile);
        return this;
    }

    public String getFullName() {
        return getProfile().getFirstName() + " " + getProfile().getLastName();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Customer setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;

    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Customer setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;

    }

    @Override
    public String toString() {
        return "Customer {" +
               " id=" + id +
               ", email='" + email + '\'' +
               ", account_id=" + account.getId() +
               ", account_status=" + account.getAccountStatus() +
               ", profile=" + profile.getId() +
               ", role=" + role +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';

    }
}
