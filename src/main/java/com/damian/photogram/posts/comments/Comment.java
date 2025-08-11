package com.damian.photogram.posts.comments;

import com.damian.photogram.customers.Customer;
import com.damian.photogram.posts.post.Post;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_post_comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @Column
    private String comment;

    @Column
    private Instant createdAt;

    public Comment() {
        this.createdAt = Instant.now();
    }

    public Comment(Customer postCustomer) {
        this();
        this.customer = postCustomer;
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
        return "Comment {" +
               " id=" + id +
               ", customerId=" + customer.getId() +
               ", comment=" + comment +
               ", createdAt=" + createdAt +
               "}";
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
