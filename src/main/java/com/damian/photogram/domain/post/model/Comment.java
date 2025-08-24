package com.damian.photogram.domain.post.model;

import com.damian.photogram.domain.customer.model.Customer;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_post_comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer author;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    @Column
    private String comment;

    @Column
    private Instant createdAt;

    public Comment() {
        this.createdAt = Instant.now();
    }

    public Comment(Customer author, Post post) {
        this(author);
        this.post = post;
    }

    public Comment(Customer postCustomer) {
        this();
        this.author = postCustomer;
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
               ", authorCustomerId=" + author.getId() +
               ", comment=" + comment +
               ", createdAt=" + createdAt +
               "}";
    }

    public Customer getAuthor() {
        return author;
    }

    public void setAuthor(Customer author) {
        this.author = author;
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

    public boolean isAuthor(Customer customer) {
        return this.author.getId().equals(customer.getId());
    }
}
