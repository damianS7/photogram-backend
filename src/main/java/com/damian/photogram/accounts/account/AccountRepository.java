package com.damian.photogram.accounts.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCustomer_Id(Long customerId);

    Optional<Account> findByCustomer_Email(String email);
}

