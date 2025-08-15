package com.damian.photogram.accounts.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByCustomer_Id(Long customerId);

    Optional<AccountToken> findByToken(String token);
}

