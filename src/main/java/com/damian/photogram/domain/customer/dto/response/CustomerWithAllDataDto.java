package com.damian.photogram.domain.customer.dto.response;

import com.damian.photogram.domain.customer.enums.CustomerRole;

import java.time.Instant;

public record CustomerWithAllDataDto(
        Long id,
        String email,
        CustomerRole role,
        ProfileDto profile,
        Instant createdAt,
        Instant updatedAt
) {
}