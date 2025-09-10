package com.damian.photogram.domain.setting;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.domain.customer.enums.UserRole;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.setting.dto.SettingDto;
import com.damian.photogram.domain.setting.dto.SettingUpdateRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SettingIntegrationTest extends AbstractIntegrationTest {

    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setEmail("customerA@test.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(UserRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);
    }


    @Test
    @DisplayName("Should get logged customer settings")
    void shouldGetSettings() throws Exception {
        // given
        loginWithCustomer(customer);

        Setting setting1 = new Setting(customer, "lang", "en");
        Setting setting2 = new Setting(customer, "2fa", "enabled");

        settingRepository.save(setting1);
        settingRepository.save(setting2);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/settings")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        SettingDto[] settings = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                SettingDto[].class
        );

        // then
        assertThat(settings).isNotNull();
        assertEquals(2, settings.length);
    }

    @Test
    @DisplayName("Should update customer settings")
    void shouldUpdateSettings() throws Exception {
        // given
        loginWithCustomer(customer);

        Setting setting1 = new Setting(customer, "lang", "en");
        settingRepository.save(setting1);

        SettingUpdateRequest request = new SettingUpdateRequest(
                "es"
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        put("/api/v1/settings/{id}", setting1.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        SettingDto settings = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                SettingDto.class
        );

        // then
        assertThat(settings).isNotNull();
        assertThat(settings).extracting("value").isEqualTo(request.value());
    }
}