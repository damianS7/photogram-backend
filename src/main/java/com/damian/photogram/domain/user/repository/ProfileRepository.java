package com.damian.photogram.domain.user.repository;

import com.damian.photogram.domain.user.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUsernameIgnoreCase(String username);

    Optional<Profile> findByCustomer_Id(Long customerId);

    boolean existsByUsernameIgnoreCase(String username);
}

