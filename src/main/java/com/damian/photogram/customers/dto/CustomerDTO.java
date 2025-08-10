package com.damian.photogram.customers.dto;

import com.damian.photogram.customers.CustomerRole;

import java.time.Instant;

public record CustomerDTO(
        Long id,
        String email,
        CustomerRole role,
        Instant createdAt,
        Instant updatedAt
) {
}