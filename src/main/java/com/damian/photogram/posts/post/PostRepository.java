package com.damian.photogram.posts.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Set<Post> findAllByCustomerId(Long customerId);

    @Query("SELECT p FROM Post p WHERE p.customer.profile.username = :username")
    Set<Post> findAllByUsername(String username);

    // count the number of posts for a specific customer
    @Query("SELECT COUNT(p) FROM Post p WHERE p.customer.id = :customerId")
    Long countByCustomerId(Long customerId);
}

