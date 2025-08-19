package com.damian.photogram.domain.post.repository;

import com.damian.photogram.domain.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Set<Post> findAllByAuthorId(Long authorId);

    @Query("SELECT p FROM Post p WHERE p.author.profile.username = :username")
    Page<Post> findAllByUsername(String username, Pageable pageable);

    // count the number of post for a specific author
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId")
    Long countByAuthorId(Long authorId);
}

