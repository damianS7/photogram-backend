package com.damian.photogram.domain.user.customer.dto.response;

import com.damian.photogram.domain.user.customer.enums.UserRole;

import java.time.Instant;

public record CustomerDto(
        Long id,
        String email,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}