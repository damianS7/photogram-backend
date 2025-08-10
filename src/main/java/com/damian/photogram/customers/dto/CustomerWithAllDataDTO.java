package com.damian.photogram.customers.dto;

import com.damian.photogram.customers.CustomerRole;
import com.damian.photogram.customers.profile.ProfileDTO;

import java.time.Instant;

public record CustomerWithAllDataDTO(
        Long id,
        String email,
        CustomerRole role,
        ProfileDTO profile,
        Instant createdAt,
        Instant updatedAt
) {
}