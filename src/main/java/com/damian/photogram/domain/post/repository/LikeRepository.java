package com.damian.photogram.domain.post.repository;

import com.damian.photogram.domain.post.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("SELECT COUNT(c) > 0 FROM Like c WHERE c.post.id = :postId AND c.customer.id = :customerId")
    boolean isPostLikedByCustomer(
            @Param("postId") Long postId,
            @Param("customerId") Long customerId
    );

    @Query("SELECT c FROM Like c WHERE c.post.id = :postId AND c.customer.id = :customerId")
    Optional<Like> findByPostIdAndCustomerId(
            @Param("postId") Long postId,
            @Param("customerId") Long customerId
    );


    @Query("SELECT COUNT(c) FROM Like c WHERE c.post.id = :postId")
    Long countLikesFromPost(@Param("postId") Long postId);
}

