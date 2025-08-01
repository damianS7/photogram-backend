package com.damian.photogram.follower;

import com.damian.photogram.customer.Customer;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_friends")
public class Follower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "friend_customer_id", referencedColumnName = "id")
    private Customer friend;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public Follower() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Follower(Customer customer, Customer friend) {
        this();
        this.customer = customer;
        this.friend = friend;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "Friend {" +
               "id=" + id +
               "customerId=" + customer.getId() +
               "friendCustomerId=" + friend.getId() +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               "}";
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getFriend() {
        return friend;
    }

    public void setFriend(Customer friend) {
        this.friend = friend;
    }
}
