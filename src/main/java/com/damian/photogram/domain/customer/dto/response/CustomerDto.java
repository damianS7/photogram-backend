package com.damian.photogram.domain.customer.dto.response;

import com.damian.photogram.domain.customer.enums.CustomerRole;

import java.time.Instant;

public record CustomerDto(
        Long id,
        String email,
        CustomerRole role,
        Instant createdAt,
        Instant updatedAt
) {
}