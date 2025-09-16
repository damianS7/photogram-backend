package com.damian.photogram.web.user.dto.response;

import com.damian.photogram.domain.user.enums.UserRole;

import java.time.Instant;

public record CustomerWithAllDataDto(
        Long id,
        String email,
        UserRole role,
        ProfileDto profile,
        Instant createdAt,
        Instant updatedAt
) {
}