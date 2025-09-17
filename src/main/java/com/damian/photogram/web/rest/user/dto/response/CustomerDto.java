package com.damian.photogram.web.rest.user.dto.response;

import com.damian.photogram.domain.user.enums.UserRole;

import java.time.Instant;

public record CustomerDto(
        Long id,
        String email,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}