package com.damian.photogram.domain.setting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request used to update settings.
 *
 * @param settings a Map containing settingId: settingValue
 */
public record SettingsPatchRequest(
        @NotNull(message = "Settings map cannot be null")
        @NotEmpty(message = "Settings map must have at least one entry")
        Map<@NotNull(message = "Setting id cannot be null") Long,
                @NotBlank(message = "Setting value cannot be blank") String> settings
) {
}
