package com.damian.photogram.domain.customer.dto.response;

import com.damian.photogram.app.user.UserRole;

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