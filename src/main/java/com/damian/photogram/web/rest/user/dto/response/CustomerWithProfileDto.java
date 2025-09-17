package com.damian.photogram.web.rest.user.dto.response;

import com.damian.photogram.domain.user.enums.UserRole;

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