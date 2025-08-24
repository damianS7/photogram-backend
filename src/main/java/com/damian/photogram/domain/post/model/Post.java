package com.damian.photogram.domain.post.model;

import com.damian.photogram.domain.customer.model.Customer;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customer_posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer author;

    @Column
    private String photoFilename;

    @Column
    private String description;

    @Column
    private Instant createdAt;

    public Post() {
        this.createdAt = Instant.now();
    }

    public Post(Customer author) {
        this();
        this.author = author;
    }

    public static Post create(Customer author) {
        Post post = new Post();
        post.setAuthor(author);
        return post;
    }

    public Long getId() {
        return id;
    }

    public Post setId(Long id) {
        this.id = id;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Post setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return "Post {" +
               "id=" + id +
               "authorCustomerId=" + author.getId() +
               ", createdAt=" + createdAt +
               "}";
    }

    public Customer getAuthor() {
        return author;
    }

    public Post setAuthor(Customer author) {
        this.author = author;
        return this;
    }

    public String getPhotoFilename() {
        return photoFilename;
    }

    public Post setPhotoFilename(String photoFilename) {
        this.photoFilename = photoFilename;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Post setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isAuthor(Customer customer) {
        // check if the customer is the author of the post.
        return this.author.getId().equals(customer.getId());
    }
}
