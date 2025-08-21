package com.damian.photogram.domain.customer.repository;

import com.damian.photogram.domain.customer.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Page<Follow> findAllByFollowedCustomer_Id(Long followedCustomerId, Pageable pageable);

    Page<Follow> findAllByFollowerCustomer_Id(Long followerCustomerId, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Follow c WHERE c.followedCustomer.id = :followedCustomerId AND c.followerCustomer.id = :followerCustomerId")
    boolean isFollowing(
            @Param("followedCustomerId") Long followedCustomerId,
            @Param("followerCustomerId") Long followerCustomerId
    );

    @Query("SELECT c FROM Follow c WHERE c.followedCustomer.id = :followedCustomerId AND c.followerCustomer.id = :followerCustomerId")
    Optional<Follow> findFollowRelationshipBetweenCustomers(
            @Param("followedCustomerId") Long followedCustomerId,
            @Param("followerCustomerId") Long followerCustomerId
    );

    @Query("SELECT COUNT(c) FROM Follow c WHERE c.followedCustomer.id = :customerId")
    Long countFollowersFromCustomer(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(c) FROM Follow c WHERE c.followerCustomer.id = :customerId")
    Long countFollowsFromCustomer(@Param("customerId") Long customerId);
}

