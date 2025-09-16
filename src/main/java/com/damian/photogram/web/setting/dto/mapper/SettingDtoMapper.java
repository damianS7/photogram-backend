package com.damian.photogram.web.setting.dto.mapper;

import com.damian.photogram.web.setting.dto.response.SettingDto;
import com.damian.photogram.domain.setting.Setting;

import java.util.Set;
import java.util.stream.Collectors;

public class SettingDtoMapper {
    public static SettingDto toSettingDTO(Setting setting) {
        return new SettingDto(
                setting.getId(),
                setting.getSettingKey(),
                setting.getSettingValue()
        );
    }

    public static Set<SettingDto> toSettingDTOList(Set<Setting> settings) {
        return settings
                .stream()
                .map(
                        SettingDtoMapper::toSettingDTO
                ).collect(Collectors.toSet());
    }
}
