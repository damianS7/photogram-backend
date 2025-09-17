package com.damian.photogram.service.setting;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.setting.Setting;
import com.damian.photogram.domain.setting.SettingRepository;
import com.damian.photogram.domain.setting.exception.SettingNotFoundException;
import com.damian.photogram.domain.setting.exception.SettingNotOwnerException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.setting.dto.SettingUpdateRequest;
import com.damian.photogram.web.rest.setting.dto.SettingsPatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SettingService {
    private static final Logger log = LoggerFactory.getLogger(SettingService.class);
    private final SettingRepository settingRepository;

    public SettingService(
            SettingRepository settingRepository
    ) {
        this.settingRepository = settingRepository;
    }

    // get all the settings for the current customer
    public Set<Setting> getSettings() {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        return settingRepository.findByCustomer_Id(currentCustomer.getId());
    }

    // update only one setting
    public Setting updateSetting(Long settingId, SettingUpdateRequest request) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // find the setting by settingId
        Setting setting = settingRepository.findById(settingId).orElseThrow(
                () -> new SettingNotFoundException(Exceptions.SETTINGS.NOT_FOUND, settingId)
        );

        // check if the logged customer is the owner of the setting.
        if (!setting.isOwner(currentCustomer)) {
            throw new SettingNotOwnerException(Exceptions.SETTINGS.NOT_OWNER, currentCustomer.getId());
        }

        setting.setSettingValue(request.value());

        log.debug(
                "Updated setting: {} with value: {} by customer: {}",
                setting.getSettingKey(),
                setting.getSettingValue(),
                currentCustomer.getId()
        );
        
        return settingRepository.save(setting);
    }

    // update multiple settings at once
    public Set<Setting> updateSettings(SettingsPatchRequest request) {
        request.settings().forEach((id, value) -> {
            this.updateSetting(id, new SettingUpdateRequest(value));
        });

        return this.getSettings();
    }
}
