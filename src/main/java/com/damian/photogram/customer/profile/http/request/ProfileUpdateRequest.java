package com.damian.photogram.customer.profile.http.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ProfileUpdateRequest(
        @NotBlank
        String currentPassword,

        Map<String, Object> fieldsToUpdate
) {
}
