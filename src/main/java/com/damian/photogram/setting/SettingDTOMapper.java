package com.damian.photogram.setting;

import com.damian.photogram.setting.dto.SettingDTO;

import java.util.Set;
import java.util.stream.Collectors;

public class SettingDTOMapper {
    public static SettingDTO toSettingDTO(Setting setting) {
        return new SettingDTO(
                setting.getId(),
                setting.getKey(),
                setting.getValue()
        );
    }

    public static Set<SettingDTO> toSettingDTOList(Set<Setting> settings) {
        return settings
                .stream()
                .map(
                        SettingDTOMapper::toSettingDTO
                ).collect(Collectors.toSet());
    }
}
