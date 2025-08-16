package com.damian.photogram.domain.customer.dto.response;

import com.damian.photogram.domain.customer.enums.CustomerGender;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String phone,
        LocalDate birthdate,
        CustomerGender gender,
        String avatarFilename,
        Instant updatedAt
) {
}
