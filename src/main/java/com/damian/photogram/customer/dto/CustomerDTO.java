package com.damian.photogram.customer.dto;

import com.damian.photogram.customer.CustomerRole;

import java.time.Instant;

public record CustomerDTO(
        Long id,
        String email,
        CustomerRole role,
        Instant createdAt,
        Instant updatedAt
) {
}