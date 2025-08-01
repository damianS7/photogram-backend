package com.damian.photogram.setting.http;

import java.util.Map;

public record SettingsPatchRequest(
        // id / value
        Map<Long, String> settings
) {
}
