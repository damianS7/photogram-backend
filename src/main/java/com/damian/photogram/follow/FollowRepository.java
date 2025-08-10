package com.damian.photogram.follow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Set<Follow> findAllByFollowedCustomer_Id(Long followedCustomerId);

    Set<Follow> findAllByFollowerCustomer_Id(Long followerCustomerId);

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
    //    Long countByFollowedCustomer_Id(@Param("followedCustomerId") Long followedCustomerId);

    @Query("SELECT COUNT(c) FROM Follow c WHERE c.followerCustomer.id = :customerId")
    Long countFollowsFromCustomer(@Param("customerId") Long customerId);
    //    Long countByFollowerCustomer_Id(@Param("followerCustomerId") Long followerCustomerId);
}

