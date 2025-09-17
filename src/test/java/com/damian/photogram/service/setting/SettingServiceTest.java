package com.damian.photogram.service.setting;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.domain.setting.Setting;
import com.damian.photogram.domain.setting.SettingRepository;
import com.damian.photogram.domain.setting.exception.SettingNotFoundException;
import com.damian.photogram.domain.setting.exception.SettingNotOwnerException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.setting.dto.SettingUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SettingServiceTest extends AbstractServiceTest {

    @Mock
    private SettingRepository settingRepository;

    @InjectMocks
    private SettingService settingService;

    @Test
    @DisplayName("Should get settings for the current customer")
    void shouldGetSettings() {
        // given
        Customer currentCustomer = Customer
                .create()
                .setId(1L)
                .setEmail("customer@demo.com")
                .setPassword(passwordEncoder.encode(RAW_PASSWORD)
                );
        setUpContext(currentCustomer);

        Set<Setting> givenSettings = Set.of(
                new Setting(currentCustomer, "key1", "value"),
                new Setting(currentCustomer, "key2", "value")
        );

        // when
        when(settingRepository.findByCustomer_Id(currentCustomer.getId())).thenReturn(givenSettings);
        Set<Setting> result = settingService.getSettings();

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(Setting::getSettingKey)
                .containsExactlyInAnyOrder("key1", "key2");
        verify(settingRepository, times(1)).findByCustomer_Id(currentCustomer.getId());
    }

    @Test
    @DisplayName("Should update a setting")
    void shouldUpdateSetting() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@demo.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD)
                                           );
        setUpContext(currentCustomer);

        Setting givenSetting = Setting.create(currentCustomer)
                                      .setId(3L)
                                      .setSettingKey("key1")
                                      .setSettingValue("value");

        SettingUpdateRequest request = new SettingUpdateRequest("newValue");

        // when
        when(settingRepository.findById(givenSetting.getId())).thenReturn(Optional.of(givenSetting));
        when(settingRepository.save(any(Setting.class))).thenReturn(givenSetting);
        Setting settingResult = settingService.updateSetting(givenSetting.getId(), request);

        // then
        assertThat(settingResult)
                .isNotNull()
                .extracting(Setting::getSettingValue)
                .isEqualTo(request.value());
        verify(settingRepository, times(1)).findById(givenSetting.getId());
        verify(settingRepository, times(1)).save(any(Setting.class));
    }

    @Test
    @DisplayName("Should not update a setting when not found")
    void shouldNotUpdateSettingWhenNotFound() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@demo.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD)
                                           );
        setUpContext(currentCustomer);

        Setting givenSetting = Setting.create(currentCustomer)
                                      .setId(3L)
                                      .setSettingKey("key1")
                                      .setSettingValue("value");
        SettingUpdateRequest request = new SettingUpdateRequest("newValue");

        // when
        when(settingRepository.findById(givenSetting.getId())).thenReturn(Optional.empty());

        assertThrows(
                SettingNotFoundException.class,
                () -> settingService.updateSetting(givenSetting.getId(), request)
        );

        // then
        verify(settingRepository, times(1)).findById(givenSetting.getId());
    }

    @Test
    @DisplayName("Should not update a setting when not owner")
    void shouldNotUpdateSettingWhenNotOwner() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@demo.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD)
                                           );
        setUpContext(currentCustomer);

        Customer settingOwner = Customer.create()
                                        .setId(5L)
                                        .setEmail("customer2@demo.com")
                                        .setPassword(passwordEncoder.encode(RAW_PASSWORD)
                                        );

        Setting givenSetting = Setting.create(settingOwner)
                                      .setId(3L)
                                      .setSettingKey("key")
                                      .setSettingValue("value");

        SettingUpdateRequest request = new SettingUpdateRequest("newValue");

        // when
        when(settingRepository.findById(givenSetting.getId())).thenReturn(Optional.of(givenSetting));

        assertThrows(
                SettingNotOwnerException.class,
                () -> settingService.updateSetting(givenSetting.getId(), request)
        );

        // then
        verify(settingRepository, times(1)).findById(givenSetting.getId());
    }
}
