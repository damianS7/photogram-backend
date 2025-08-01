package com.damian.photogram.setting;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.setting.exception.SettingAuthorizationException;
import com.damian.photogram.setting.exception.SettingNotFoundException;
import com.damian.photogram.setting.http.SettingUpdateRequest;
import com.damian.photogram.setting.http.SettingsPatchRequest;
import org.springframework.stereotype.Service;

import java.util.Set;
// TODO settings by customer!!!

@Service
public class SettingService {
    private final SettingRepository settingRepository;

    public SettingService(
            SettingRepository settingRepository
    ) {
        this.settingRepository = settingRepository;
    }

    // get all the friends for the logged customer
    public Set<Setting> getSettings() {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return settingRepository.findByCustomer_Id(loggedCustomer.getId());
    }

    public Setting updateSetting(Long id, SettingUpdateRequest request) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        Setting setting = settingRepository.findById(id).orElseThrow(
                () -> new SettingNotFoundException(Exceptions.SETTINGS.NOT_FOUND)
        );

        // check if the logged customer is the owner of the setting.
        if (!loggedCustomer.getId().equals(setting.getCustomer().getId())) {
            throw new SettingAuthorizationException(Exceptions.SETTINGS.NOT_OWNER);
        }

        setting.setValue(request.value());
        return settingRepository.save(setting);
    }

    public Set<Setting> updateSettings(SettingsPatchRequest request) {
        request.settings().forEach((id, value) -> {
            this.updateSetting(id, new SettingUpdateRequest(value));
        });

        return this.getSettings();
    }

}
