package com.damian.photogram.customers.profile;

import com.damian.photogram.customers.CustomerGender;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileDTO(
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
