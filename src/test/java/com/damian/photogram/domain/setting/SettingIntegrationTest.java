package com.damian.photogram.domain.setting;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.setting.dto.SettingDto;
import com.damian.photogram.domain.setting.dto.SettingUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SettingIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customer;
    private String token;

    @BeforeAll
    void setUp() {
        customerRepository.deleteAll();

        customer = new Customer();
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode("123456"));
        customer.getAccount().setAccountStatus(AccountStatus.ACTIVE);

        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customer);
    }

    @AfterAll
    void tearDown() {
        settingRepository.deleteAll();
        customerRepository.deleteAll();
    }

    void loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        token = response.token();
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