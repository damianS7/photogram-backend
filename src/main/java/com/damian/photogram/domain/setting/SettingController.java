package com.damian.photogram.domain.setting;

import com.damian.photogram.domain.setting.dto.SettingDto;
import com.damian.photogram.domain.setting.dto.SettingUpdateRequest;
import com.damian.photogram.domain.setting.dto.SettingsPatchRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/api/v1")
@RestController
public class SettingController {
    private final SettingService settingService;

    @Autowired
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    // endpoint to fetch all setting from logged customer
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        Set<Setting> settings = settingService.getSettings();
        Set<SettingDto> settingsDTO = SettingDtoMapper.toSettingDTOList(settings);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(settingsDTO);
    }

    // endpoint to update a setting
    @PutMapping("/settings/{id}")
    public ResponseEntity<?> updateSetting(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            SettingUpdateRequest request
    ) {
        Setting setting = settingService.updateSetting(id, request);
        SettingDto settingDTO = SettingDtoMapper.toSettingDTO(setting);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(settingDTO);
    }

    // endpoint to update a setting
    @PatchMapping("/settings")
    public ResponseEntity<?> updateSettings(
            @Validated @RequestBody
            SettingsPatchRequest request
    ) {
        Set<Setting> setting = settingService.updateSettings(request);
        Set<SettingDto> settingDTO = SettingDtoMapper.toSettingDTOList(setting);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(settingDTO);
    }
}

