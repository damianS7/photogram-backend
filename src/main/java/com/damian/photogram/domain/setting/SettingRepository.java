package com.damian.photogram.domain.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    Set<Setting> findByCustomer_Id(Long customerId);
}

