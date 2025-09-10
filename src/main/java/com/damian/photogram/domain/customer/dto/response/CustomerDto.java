package com.damian.photogram.domain.customer.dto.response;

import com.damian.photogram.domain.customer.enums.UserRole;

import java.time.Instant;

public record CustomerDto(
        Long id,
        String email,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}