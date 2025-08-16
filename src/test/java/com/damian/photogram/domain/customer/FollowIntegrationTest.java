package com.damian.photogram.domain.customer;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.dto.response.FollowDto;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Follow;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.repository.FollowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class FollowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customer;
    private String token;

    @BeforeEach
    void setUp() {
        followRepository.deleteAll();
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
    @DisplayName("Should get followers")
    void shouldGetFollowers() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFollower = new Customer(
                "follow@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFollower);
        followRepository.save(
                new Follow(customer, customerFollower)
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/customers/me/followers")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        FollowDto[] followDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FollowDto[].class
        );

        // then
        assertThat(followDto).isNotNull();
        assertThat(followDto.length).isGreaterThanOrEqualTo(1);
        assertThat(followDto[0].followedCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    @DisplayName("Should follow")
    void shouldFollow() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerToBeFollowed = new Customer(
                "follow@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerToBeFollowed);

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/customers/{customerId}/follow", customerToBeFollowed.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        FollowDto followDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FollowDto.class
        );

        // then
        assertThat(followDto).isNotNull();
        assertEquals(followDto.followedCustomerId(), customerToBeFollowed.getId());
    }

    @Test
    @DisplayName("Should not add a follow when already exists")
    void shouldNotFollowWhenAlreadyExists() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerToBeFollowed = new Customer(
                "follow@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerToBeFollowed);

        Follow follow = new Follow(customerToBeFollowed, customer);
        followRepository.save(follow);

        // when
        mockMvc
                .perform(
                        post("/api/v1/customers/{customerId}/follow", customerToBeFollowed.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(409))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    @DisplayName("Should not follow when customer not found")
    void shouldNotFollowWhenCustomerNotFound() throws Exception {
        // given
        loginWithCustomer(customer);

        // when
        mockMvc
                .perform(
                        post("/api/v1/customers/{customerId}/follow", 99L)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(404))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    @DisplayName("Should unfollow")
    void shouldUnfollow() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFollowed = new Customer(
                "follow@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFollowed);

        Follow givenFollow = new Follow(customerFollowed, customer);
        followRepository.save(givenFollow);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/customers/{id}/unfollow", customerFollowed.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(204))
                .andReturn();

        // then
    }

    @Test
    @DisplayName("Should not unfollow when not following")
    void shouldNotUnfollowWhenNotFollowing() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFriend = new Customer(
                "follow@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/customers/{id}/unfollow", 99L)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(404))
                .andReturn();

        // then
    }
}
