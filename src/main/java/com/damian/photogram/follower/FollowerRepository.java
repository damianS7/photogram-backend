package com.damian.photogram.follower;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {
    Set<Follower> findAllByCustomerId(Long customerId);

    @Query("SELECT COUNT(c) > 0 FROM Friend c WHERE c.customer.id = :customerId AND c.follower.id = :friendCustomerId")
    boolean friendExists(@Param("customerId") Long customerId, @Param("friendCustomerId") Long friendCustomerId);
}

