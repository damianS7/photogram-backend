package com.damian.photogram.domain.customer.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ProfileUpdateRequest(
        @NotBlank
        String currentPassword,

        Map<String, Object> fieldsToUpdate
) {
}
