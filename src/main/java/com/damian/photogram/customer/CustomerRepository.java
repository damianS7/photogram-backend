package com.damian.photogram.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    @Query(
            """
                        SELECT c
                        FROM   Customer c
                        JOIN   c.profile p
                        WHERE  LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))
                    """
    )
    Set<Customer> findTop10ByName(@Param("firstName") String firstName);
}

