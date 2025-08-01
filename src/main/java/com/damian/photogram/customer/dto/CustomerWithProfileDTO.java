package com.damian.photogram.customer.dto;

import com.damian.photogram.customer.CustomerRole;
import com.damian.photogram.customer.profile.ProfileDTO;

import java.time.Instant;

public record CustomerWithProfileDTO(
        Long id,
        String email,
        CustomerRole role,
        ProfileDTO profile,
        Instant createdAt,
        Instant updatedAt
) {
}