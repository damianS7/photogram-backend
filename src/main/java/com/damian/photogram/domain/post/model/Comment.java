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

    public static Comment create(Customer author, Post post) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        return comment;
    }

    public Long getId() {
        return id;
    }

    public Comment setId(Long id) {
        this.id = id;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Comment setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return "Comment {" +
               " id=" + id +
               ", postId=" + post.getId() +
               ", authorId=" + author.getId() +
               ", comment=" + comment +
               ", createdAt=" + createdAt +
               "}";
    }

    public Customer getAuthor() {
        return author;
    }

    public Comment setAuthor(Customer author) {
        this.author = author;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Comment setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Post getPost() {
        return post;
    }

    public Comment setPost(Post post) {
        this.post = post;
        return this;
    }

    // check if the given customer is the author of the comment
    public boolean isAuthor(Customer customer) {
        return this.author.getId().equals(customer.getId());
    }
}
