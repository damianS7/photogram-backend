package com.damian.photogram.service.feed;

import com.damian.photogram.web.feed.dto.response.FeedDto;
import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FeedIntegrationTest extends AbstractIntegrationTest {
    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = new Customer();
        customer.setRole(UserRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode("123456"));
        customer.getProfile().setUsername("customer7777");
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);

        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customer);
    }

    @Test
    @DisplayName("Should get feed")
    void shouldGetFeed() throws Exception {
        // given
        loginWithCustomer(customer);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/customers/{username}/feed", customer.getProfile().getUsername())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        FeedDto feedDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FeedDto.class
        );

        // then
        assertThat(feedDto).isNotNull();
    }
}