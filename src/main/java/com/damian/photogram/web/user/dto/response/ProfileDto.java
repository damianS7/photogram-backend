package com.damian.photogram.web.user.dto.response;

import com.damian.photogram.domain.user.enums.CustomerGender;

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
