package com.damian.photogram.domain.setting.dto;

import java.util.Map;

public record SettingsPatchRequest(
        // id / value
        Map<Long, String> settings
) {
}
