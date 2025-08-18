package com.damian.photogram.domain.customer.repository;

import com.damian.photogram.domain.customer.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUsername(String username);

    Optional<Profile> findByCustomer_Id(Long customerId);
}

