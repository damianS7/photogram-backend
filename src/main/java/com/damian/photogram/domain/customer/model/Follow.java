package com.damian.photogram.domain.customer.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_follows")
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_customer_id", referencedColumnName = "id")
    private Customer followedCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_customer_id", referencedColumnName = "id")
    private Customer followerCustomer;

    @Column
    private Instant createdAt;

    public Follow() {
        this.createdAt = Instant.now();
    }

    public Follow(Customer followedCustomer, Customer followerCustomer) {
        this();
        this.followedCustomer = followedCustomer;
        this.followerCustomer = followerCustomer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Follow {" +
               "id=" + id +
               ", followedCustomerId=" + followedCustomer.getId() +
               ", followerCustomerId=" + followerCustomer.getId() +
               ", createdAt=" + createdAt +
               "}";
    }

    public Customer getFollowedCustomer() {
        return followedCustomer;
    }

    public void setFollowedCustomer(Customer followedCustomer) {
        this.followedCustomer = followedCustomer;
    }

    public Customer getFollowerCustomer() {
        return followerCustomer;
    }

    public void setFollowerCustomer(Customer followerCustomer) {
        this.followerCustomer = followerCustomer;
    }
}
