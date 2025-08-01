package com.damian.photogram.setting;

import com.damian.photogram.setting.dto.SettingDTO;
import com.damian.photogram.setting.http.SettingUpdateRequest;
import com.damian.photogram.setting.http.SettingsPatchRequest;
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

    // endpoint to fetch all settings from logged customer
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        Set<Setting> settings = settingService.getSettings();
        Set<SettingDTO> settingsDTO = SettingDTOMapper.toSettingDTOList(settings);

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
        SettingDTO settingDTO = SettingDTOMapper.toSettingDTO(setting);

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
        Set<SettingDTO> settingDTO = SettingDTOMapper.toSettingDTOList(setting);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(settingDTO);
    }
}

