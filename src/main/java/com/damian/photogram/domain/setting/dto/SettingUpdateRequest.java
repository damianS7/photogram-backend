package com.damian.photogram.domain.setting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SettingUpdateRequest(
        @NotNull(message = "value cannot be null")
        @NotBlank(message = "value cannot be blank")
        String value
) {
}
