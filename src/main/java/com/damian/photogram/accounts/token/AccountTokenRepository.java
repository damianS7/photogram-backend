package com.damian.photogram.accounts.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    /**
     * Finds an Auth instance by the customer_id
     *
     * @param customerId is the id of the customers
     * @return an Optional containing the Auth instance if found
     */
    Optional<AccountToken> findByCustomer_Id(Long customerId);

    Optional<AccountToken> findByToken(String token);
}

