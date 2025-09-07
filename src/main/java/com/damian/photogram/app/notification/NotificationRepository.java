package com.damian.photogram.app.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByCustomerId(Long customerId, Pageable pageable);

    void deleteAllByCustomer_Id(Long customerId);
}

