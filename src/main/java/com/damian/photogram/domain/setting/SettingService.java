package com.damian.photogram.domain.setting;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.setting.dto.SettingUpdateRequest;
import com.damian.photogram.domain.setting.dto.SettingsPatchRequest;
import com.damian.photogram.domain.setting.exception.SettingAuthorizationException;
import com.damian.photogram.domain.setting.exception.SettingNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SettingService {
    private final SettingRepository settingRepository;

    public SettingService(
            SettingRepository settingRepository
    ) {
        this.settingRepository = settingRepository;
    }

    // get all the settings for the logged customer
    public Set<Setting> getSettings() {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return settingRepository.findByCustomer_Id(loggedCustomer.getId());
    }

    // update only one setting
    public Setting updateSetting(Long id, SettingUpdateRequest request) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // find the setting by id
        Setting setting = settingRepository.findById(id).orElseThrow(
                () -> new SettingNotFoundException(Exceptions.SETTINGS.NOT_FOUND)
        );

        // check if the logged customer is the owner of the setting.
        if (!loggedCustomer.getId().equals(setting.getCustomer().getId())) {
            throw new SettingAuthorizationException(Exceptions.SETTINGS.NOT_OWNER);
        }

        setting.setSettingValue(request.value());
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
