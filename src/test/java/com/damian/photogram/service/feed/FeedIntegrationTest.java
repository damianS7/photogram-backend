package com.damian.photogram.service.feed;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.ApiResponse;
import com.damian.photogram.core.util.JsonHelper;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.feed.dto.response.FeedDto;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        customer = Customer.create()
                           .setEmail("customer@test.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(UserRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setUsername("johnwick")
                                   .setAboutMe("hello im john")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
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
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        FeedDto feedDto = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                FeedDto.class
        );

        // then
        assertThat(feedDto)
                .isNotNull()
                .extracting(
                        FeedDto::customerId,
                        FeedDto::totalPosts,
                        FeedDto::followers,
                        FeedDto::following,
                        FeedDto::username,
                        FeedDto::aboutMe,
                        FeedDto::profileImageFilename
                ).containsExactly(
                        customer.getId(),
                        0L,
                        0L,
                        0L,
                        customer.getUsername(),
                        customer.getProfile().getAboutMe(),
                        customer.getProfile().getImageFilename()
                );
    }

    @Test
    @DisplayName("Should not get feed when username not exists")
    void shouldNotGetFeedWhenUsernameProfileNotExists() throws Exception {
        // given
        loginWithCustomer(customer);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/customers/{username}/feed", "non-exist-username")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        ApiResponse<?> response = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<?>>() {
                }
        );
        // then
        assertThat(response)
                .isNotNull()
                .extracting(
                        ApiResponse::getMessage
                ).isEqualTo(
                        Exceptions.FEED.USER_PROFILE_NOT_FOUND
                );
    }
}