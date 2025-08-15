package com.damian.photogram.accounts.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Finds an Auth instance by the customer_id
     *
     * @param customerId is the id of the customers
     * @return an Optional containing the Auth instance if found
     */
    Optional<Account> findByCustomer_Id(Long customerId);
}

