package com.damian.photogram.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationRepository extends JpaRepository<Auth, Long> {
    /**
     * Finds an Auth instance by the customer_id
     *
     * @param customerId is the id of the customer
     * @return an Optional containing the Auth instance if found
     */
    Optional<Auth> findByCustomer_Id(Long customerId);
}

