package com.damian.photogram.domain.setting;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.setting.dto.SettingUpdateRequest;
import com.damian.photogram.domain.setting.exception.SettingNotFoundException;
import com.damian.photogram.domain.setting.exception.SettingNotOwnerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SettingServiceTest extends AbstractServiceTest {

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private SettingService settingService;

    @Test
    @DisplayName("Should get settings for the logged customer")
    void shouldGetSettings() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Set<Setting> settings = Set.of(
                new Setting(loggedCustomer, "key1", "value"),
                new Setting(loggedCustomer, "key2", "value")
        );

        // when
        when(settingRepository.findByCustomer_Id(loggedCustomer.getId())).thenReturn(settings);
        Set<Setting> result = settingService.getSettings();

        // then
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(Setting::getSettingKey)
                .containsExactlyInAnyOrder("key1", "key2");
        verify(settingRepository, times(1)).findByCustomer_Id(loggedCustomer.getId());
    }

    @Test
    @DisplayName("Should update a setting")
    void shouldUpdateSetting() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Setting setting = Setting.create(loggedCustomer)
                                 .setSettingKey("key1")
                                 .setSettingValue("value");
        setting.setId(3L);

        SettingUpdateRequest request = new SettingUpdateRequest("newValue");

        // when
        when(settingRepository.findById(setting.getId())).thenReturn(Optional.of(setting));
        when(settingRepository.save(any(Setting.class))).thenReturn(setting);
        Setting result = settingService.updateSetting(setting.getId(), request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting(Setting::getSettingValue)
                .isEqualTo(request.value());
        verify(settingRepository, times(1)).findById(setting.getId());
        verify(settingRepository, times(1)).save(any(Setting.class));
    }

    @Test
    @DisplayName("Should not update a setting when not found")
    void shouldNotUpdateSettingWhenNotFound() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Setting setting = Setting.create(loggedCustomer)
                                 .setId(3L)
                                 .setSettingKey("key")
                                 .setSettingValue("value");

        SettingUpdateRequest request = new SettingUpdateRequest("newValue");

        // when
        when(settingRepository.findById(setting.getId())).thenReturn(Optional.empty());

        assertThrows(
                SettingNotFoundException.class,
                () -> settingService.updateSetting(setting.getId(), request)
        );

        // then
        verify(settingRepository, times(1)).findById(setting.getId());
    }

    @Test
    @DisplayName("Should not update a setting when not authorized")
    void shouldNotUpdateSettingWhenNotAuthorized() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer settingOwner = new Customer(
                5L, "customer@2test.com",
                passwordEncoder.encode("password")
        );
        Setting setting = Setting.create(settingOwner)
                                 .setId(3L)
                                 .setSettingKey("key")
                                 .setSettingValue("value");

        SettingUpdateRequest request = new SettingUpdateRequest("newValue");

        // when
        when(settingRepository.findById(setting.getId())).thenReturn(Optional.of(setting));

        assertThrows(
                SettingNotOwnerException.class,
                () -> settingService.updateSetting(setting.getId(), request)
        );

        // then
        verify(settingRepository, times(1)).findById(setting.getId());
    }
}
