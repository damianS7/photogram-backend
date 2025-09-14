package com.damian.photogram.domain.user.customer.dto.response;

import com.damian.photogram.domain.user.customer.enums.UserRole;

import java.time.Instant;

public record CustomerWithProfileDto(
        Long id,
        String email,
        UserRole role,
        ProfileDto profile,
        Instant createdAt,
        Instant updatedAt
) {
}